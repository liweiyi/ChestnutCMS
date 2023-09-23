package com.chestnut.member.level;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.chestnut.member.domain.MemberLevel;
import com.chestnut.member.domain.MemberLevelConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * 等级管理器
 * 
 * @author 兮玥
 * @mail liweiyimwz@126.com
 */
@Getter
@Setter
public class LevelManager {

	/**
	 * 等级类型
	 */
	private ILevelType levelType;

	/**
	 * 按等级所需经验值降序列表
	 */
	private Map<Integer, MemberLevelConfig> levelConfigs;

	/**
	 * 最低等级
	 */
	private MemberLevelConfig firstLevelConfig;

	/**
	 * 最高等级
	 */
	private MemberLevelConfig lastLevelConfig;

	private ReentrantLock lock = new ReentrantLock();
	
	public void resetLevelConfigs(Map<Integer, MemberLevelConfig> levelConfigs) {
		lock.lock();
		try {
			this.levelConfigs = levelConfigs;
			this.firstLevelConfig = levelConfigs.get(0);
			this.lastLevelConfig = levelConfigs.get(levelConfigs.size() - 1);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 会员等级经验值变更
	 * 
	 * @param level
	 * @param exp
	 */
	public void addExp(MemberLevel level, long exp) {
		if (exp == 0) {
			return;
		}
		level.setExp(level.getExp() + exp);
		checkLevelUp(level);
	}

	private void checkLevelUp(MemberLevel level) {
		while (true) {
			MemberLevelConfig curLvConfig = this.levelConfigs.get(level.getLevel());
			if (!isMaxLevel(level.getLevel()) && level.getExp() >= curLvConfig.getNextNeedExp()) {
				long remainExp = level.getExp() - curLvConfig.getNextNeedExp();
				level.setExp(Math.max(remainExp, 0));
				int nextLevel = this.getNextLevel(level.getLevel());
				level.setLevel(nextLevel);
			} else {
				break;
			}
			this.levelType.onLevelUp(level);
		}
	}

	private int getNextLevel(int level) {
		int nextLevel = level + 1;
		while (nextLevel < this.lastLevelConfig.getLevel() && this.levelConfigs.get(nextLevel) == null) {
			nextLevel++;
		}
		return nextLevel;
	}

	private boolean isMaxLevel(int level) {
		return this.lastLevelConfig.getLevel().intValue() == level;
	}
}