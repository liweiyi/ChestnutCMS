package com.chestnut.vote.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.vote.core.IVoteUserType;
import com.chestnut.vote.domain.VoteLog;
import com.chestnut.vote.domain.dto.VoteSubmitDTO;
import com.chestnut.vote.domain.dto.VoteSubmitDTO.SubjectResult;
import com.chestnut.vote.domain.vo.VoteVO;
import com.chestnut.vote.exception.VoteErrorCode;
import com.chestnut.vote.service.IVoteApiService;
import com.chestnut.vote.service.IVoteLogService;
import com.chestnut.vote.service.IVoteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteApiServiceImpl implements IVoteApiService {

	private final IVoteService voteService;

	private final IVoteLogService voteLogService;
	
	private final RedissonClient redissonClient;
	
	private final AsyncTaskManager asyncTaskManager;

	@Override
	public VoteVO getVote(Long voteId) {
		return this.voteService.getVote(voteId);
	}

	@Override
	public void submitVote(VoteSubmitDTO dto) {
		VoteVO vote = this.getVote(dto.getVoteId());
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", dto.getVoteId()));

		IVoteUserType voteUserType = this.voteService.getVoteUserType(vote.getUserType());
		String userId = voteUserType.getUserId();
		
		// 判断投票时间
		boolean checkTime = DateUtils.isNowBetween(vote.getStartTime(), vote.getEndTime());
		Assert.isTrue(checkTime, VoteErrorCode.TIME_ERR::exception);
		
		RLock lock = redissonClient.getLock("VoteSubmit-" + userId);
		lock.lock();
		try {
			// 判断提交上限
			Long total = this.voteLogService.lambdaQuery().eq(VoteLog::getVoteId, dto.getVoteId())
					.eq(VoteLog::getUserType, vote.getUserType()).eq(VoteLog::getUserId, userId).count();
			Assert.isTrue(total < vote.getTotalLimit(), VoteErrorCode.VOTE_TOTAL_LIMIT::exception);
			
			Long dayCount = this.voteLogService.lambdaQuery().eq(VoteLog::getVoteId, dto.getVoteId())
					.eq(VoteLog::getUserType, vote.getUserType()).eq(VoteLog::getUserId, userId)
					.ge(VoteLog::getLogTime, DateUtils.getDayStart(LocalDateTime.now())).count();
			Assert.isTrue(dayCount < vote.getDayLimit(), VoteErrorCode.VOTE_DAY_LIMIT::exception);
	
			// 记录日志
			Map<Long, String> result = dto.getSubjects().stream()
					.collect(Collectors.toMap(SubjectResult::getSubjectId, SubjectResult::getResult));
			VoteLog voteLog = new VoteLog();
			voteLog.setVoteId(dto.getVoteId());
			voteLog.setUserType(vote.getUserType());
			voteLog.setUserId(userId);
			voteLog.setResult(result);
			voteLog.setLogTime(LocalDateTime.now());
			voteLog.setIp(dto.getIp());
			voteLog.setUserAgent(dto.getUserAgent());
			voteLogService.save(voteLog);
			// 更新问卷参与数和主题选项票数
			this.asyncTaskManager.execute(() -> {
				this.voteService.onVoteSubmit(voteLog);
			});
		} finally {
			lock.unlock();
		}
	}
}
