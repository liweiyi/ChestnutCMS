package com.chestnut.article.listener;

import com.chestnut.article.domain.vo.ArticleVO;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.AfterContentEditorInitEvent;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleListener {

	private final CmsArticleDetailMapper articleMapper;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除文章数据
		try {
			long total = this.articleMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除文章详情备份数据：" + (i * pageSize) + "/" + total);
				this.articleMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除文章详情错误：" + e.getMessage());
		}
	}

	@EventListener
	public void afterContentEditorInit(AfterContentEditorInitEvent event) {
		if (event.getContentVO() instanceof ArticleVO vo) {
			vo.setContentHtml(InternalUrlUtils.dealResourceInternalUrl(vo.getContentHtml()));
		}
	}
}
