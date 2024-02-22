/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.cms.stat.baidu;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.chestnut.cms.stat.baidu.dto.BaiduTimeTrendDTO;
import com.chestnut.cms.stat.baidu.vo.BaiduOverviewReportVO;
import com.chestnut.cms.stat.baidu.vo.BaiduSiteVO;
import com.chestnut.cms.stat.baidu.vo.BaiduTimeTrendVO;
import com.chestnut.common.domain.R;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;

import com.chestnut.contentcore.domain.CmsSite;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaiduTongjiUtils {

	/**
	 * 刷新AccessToken
	 */
	private static final String API_REFRESH_ACCESS_TOKEN = "http://openapi.baidu.com/oauth/2.0/token?grant_type=refresh_token&refresh_token={0}&client_id={1}&client_secret={2}";

	/**
	 * 获取站点列表（百度账号）
	 */
	private static final String API_SITE_LIST = "https://openapi.baidu.com/rest/2.0/tongji/config/getSiteList?access_token={0}";

	/**
	 * 获取站点统计数据（百度账号）
	 */
	private static final String API_DATA = "https://openapi.baidu.com/rest/2.0/tongji/report/getData?access_token={0}&site_id={1}&method={2}&start_date={3}&end_date={4}&metrics={5}";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * 刷新AccessToken
	 * 
	 * @param config
	 * @return
	 */
	public static Map<String, Object> refreshAccessToken(BaiduTongjiConfig config) {
		String url = StringUtils.messageFormat(API_REFRESH_ACCESS_TOKEN, config.getRefreshToken(),
				config.getApiKey(), config.getSecretKey());
		String result = HttpUtils.get(URI.create(url));
		return JacksonUtils.fromMap(result);
	}

	/**
	 * 站点列表
	 * 
	 * @param config
	 * @return
	 */
	public static R<List<BaiduSiteVO>> getSiteList(BaiduTongjiConfig config) {
		String url = StringUtils.messageFormat(API_SITE_LIST, config.getAccessToken());
		String responseJson = HttpUtils.get(URI.create(url));
		String errorMsg = JacksonUtils.getAsString(responseJson, "error_msg");
		if (StringUtils.isNotEmpty(errorMsg)) {
			return R.fail(errorMsg);
		}
		List<BaiduSiteVO> list = JacksonUtils.getAsList(responseJson, "list", BaiduSiteVO.class);
		return R.ok(list);
	}
	
	public static BaiduTimeTrendVO getSiteTimeTrend(String accessToken, BaiduTimeTrendDTO dto) {
		String url = StringUtils.messageFormat(API_DATA, accessToken, dto.getSiteId().toString(), "trend/time/a",
				dto.getStartDate().format(DATE_FORMAT), dto.getEndDate().format(DATE_FORMAT),
				dto.getMetrics().stream().collect(Collectors.joining(",")));
		url += "&gran=" + dto.getGran();
		if (StringUtils.isNotEmpty(dto.getArea())) {
			url += "&area=" + dto.getArea();
		}
		if (StringUtils.isNotEmpty(dto.getSource())) {
			url += "&source=" + dto.getSource();
		}
		if (StringUtils.isNotEmpty(dto.getDeviceType())) {
			url += "&deviceType=" + dto.getDeviceType();
		}
		if (StringUtils.isNotEmpty(dto.getVisitor())) {
			url += "&visitor=" + dto.getVisitor();
		}
		
		String reponseJson = HttpUtils.get(URI.create(url));
		String errorMsg = JacksonUtils.getAsString(reponseJson, "error_msg");
		if (StringUtils.isNotEmpty(errorMsg)) {
			log.error("BaiduTongji api faild: " + errorMsg);
			throw new RuntimeException(errorMsg);
		}
		JsonNode jsonNode = JacksonUtils.getAsJsonObject(reponseJson, "result");
		
		BaiduTimeTrendVO baiduTimeTrend = new BaiduTimeTrendVO();

		List<String> fields = new ArrayList<>();
		jsonNode.get("fields").forEach(jn -> {
			fields.add(jn.asText());
		});;
		baiduTimeTrend.setFields(fields);
		baiduTimeTrend.setOffset(jsonNode.get("offset").asInt());
		baiduTimeTrend.setTimeSpan(jsonNode.get("timeSpan").get(0).asText());
		baiduTimeTrend.setTotal(jsonNode.get("total").asInt());
		JsonNode jnSum = jsonNode.get("sum").get(0);
		baiduTimeTrend.setSum(new HashMap<>(fields.size() - 1));
		for (int i = 1; i < fields.size(); i++) {
			baiduTimeTrend.getSum().put(fields.get(i), jnSum.get(i - 1).asDouble());
		}
		JsonNode jnPageSum = jsonNode.get("pageSum").get(0);
		baiduTimeTrend.setPageSum(new HashMap<>(fields.size() - 1));
		for (int i = 1; i < fields.size(); i++) {
			baiduTimeTrend.getPageSum().put(fields.get(i), jnPageSum.get(i - 1).asDouble());
		}
		
		List<String> xAxisDatas = new ArrayList<>();
		jsonNode.get("items").get(0).forEach(node -> xAxisDatas.add(node.get(0).asText()));
		Collections.reverse(xAxisDatas);
		baiduTimeTrend.setXAxisDatas(xAxisDatas);
		
		Map<String, List<Object>> datas = fields.stream().filter(k -> dto.getMetrics().contains(k))
				.collect(Collectors.toMap(k -> k, k -> new ArrayList<Object>(xAxisDatas.size())));
		jsonNode.get("items").get(1).forEach(node -> {
			for (int i = 1; i < fields.size(); i++) {
				String key = fields.get(i);
				List<Object> list = datas.get(key);
				if (list != null) {
					String v = node.get(i - 1).asText();
					list.add("--".equals(v) ? "0" : v);
				}
			}
		});
		datas.values().forEach(list -> Collections.reverse(list));
		baiduTimeTrend.setDatas(datas);
		return baiduTimeTrend;
	}

	/**
	 * 站点趋势概况
	 * 
	 * @param accessToken
	 * @param siteId
	 * @param startDate
	 * @param endDate
	 */
	public static BaiduOverviewReportVO getSiteOverviewTimeTrend(String accessToken, Long siteId, LocalDateTime startDate,
			LocalDateTime endDate) {
		List<String> metrics = List.of("pv_count", "visitor_count", "ip_count", "avg_visit_time");
		String url = StringUtils.messageFormat(API_DATA, accessToken, siteId.toString(), "overview/getTimeTrendRpt",
				startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT),
				metrics.stream().collect(Collectors.joining(",")));
		String reponseJson = HttpUtils.get(URI.create(url));
		String errorMsg = JacksonUtils.getAsString(reponseJson, "error_msg");
		if (StringUtils.isNotEmpty(errorMsg)) {
			log.error("BaiduTongji api faild: " + errorMsg);
			throw new RuntimeException(errorMsg);
		}

		JsonNode jsonNode = JacksonUtils.getAsJsonObject(reponseJson, "result");

		BaiduOverviewReportVO report = new BaiduOverviewReportVO();
		List<String> fields = new ArrayList<>();
		jsonNode.get("fields").forEach(node -> fields.add(node.asText()));
		report.setFields(fields);

		JsonNode itemsNode = jsonNode.get("items");

		List<String> xAxisDatas = new ArrayList<>();
		itemsNode.get(0).forEach(node -> xAxisDatas.add(node.get(0).asText()));

		Map<String, List<Object>> datas = fields.stream().filter(k -> metrics.contains(k))
				.collect(Collectors.toMap(k -> k, k -> new ArrayList<Object>(xAxisDatas.size())));
		itemsNode.get(1).forEach(node -> {
			int index = 0;
			for (int i = 1; i < fields.size(); i++) {
				String key = fields.get(i);
				List<Object> list = datas.get(key);
				if (list != null) {
					int v = node.get(index).asInt();
					list.add(v);
					index++;
				}
			}
		});
		report.setXAxisDatas(xAxisDatas);
		report.setDatas(datas);
		return report;
	}

	/**
	 * 站点PV来源区域分布概况
	 * 
	 * @param accessToken
	 * @param siteId
	 * @param startDate
	 * @param endDate
	 */
	public static BaiduOverviewReportVO getSiteOverviewDistrict(String accessToken, Long siteId, LocalDateTime startDate,
			LocalDateTime endDate) {
		List<String> metrics = List.of("pv_count");
		String url = StringUtils.messageFormat(API_DATA, accessToken, siteId.toString(), "overview/getDistrictRpt",
				startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT),
				metrics.stream().collect(Collectors.joining(",")));
		String reponseJson = HttpUtils.get(URI.create(url));
		String errorMsg = JacksonUtils.getAsString(reponseJson, "error_msg");
		if (StringUtils.isNotEmpty(errorMsg)) {
			log.error("BaiduTongji api faild: " + errorMsg);
			throw new RuntimeException(errorMsg);
		}

		JsonNode jsonNode = JacksonUtils.getAsJsonObject(reponseJson, "result");
		
		BaiduOverviewReportVO report = new BaiduOverviewReportVO();
		List<String> fields = new ArrayList<>();
		jsonNode.get("fields").forEach(node -> fields.add(node.asText()));
		report.setFields(fields);

		JsonNode itemsNode = jsonNode.get("items");

		List<String> xAxisDatas = new ArrayList<>();
		itemsNode.get(0).forEach(node -> xAxisDatas.add(node.get(0).asText()));

		List<String> acturalMetrics = List.of("pv_count", "ratio");
		Map<String, List<Object>> datas = fields.stream().filter(k -> acturalMetrics.contains(k))
				.collect(Collectors.toMap(k -> k, k -> new ArrayList<Object>(xAxisDatas.size())));
		itemsNode.get(1).forEach(node -> {
			for (int i = 1; i < fields.size(); i++) {
				String key = fields.get(i);
				List<Object> list = datas.get(key);
				if (list != null) {
					list.add(node.get(i - 1).asText());
				}
			}
		});
		report.setXAxisDatas(xAxisDatas);
		report.setDatas(datas);
		return report;
	}

	/**
	 * 站点数据概览（来源网站、搜索词、入口页面、受访页面、新老用户）
	 * 
	 * @param accessToken
	 * @param siteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static Map<String, BaiduOverviewReportVO> getSiteOverviewOthers(String accessToken, Long siteId,
			LocalDateTime startDate, LocalDateTime endDate) {
		List<String> metrics = List.of("pv_count");
		String url = StringUtils.messageFormat(API_DATA, accessToken, siteId.toString(), "overview/getCommonTrackRpt",
				startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT),
				metrics.stream().collect(Collectors.joining(",")));
		String reponseJson = HttpUtils.get(URI.create(url));
		String errorMsg = JacksonUtils.getAsString(reponseJson, "error_msg");
		if (StringUtils.isNotEmpty(errorMsg)) {
			log.error("BaiduTongji api faild: " + errorMsg);
			throw new RuntimeException(errorMsg);
		}

		JsonNode jsonNode = JacksonUtils.getAsJsonObject(reponseJson, "result");

		// 来源网站
		JsonNode sourceSiteJsonNode = jsonNode.get("sourceSite");
		BaiduOverviewReportVO sourceSiteRpt = parseJsonToBaiduOverviewReport(sourceSiteJsonNode,
				List.of("pv_count", "ratio"));
		// 搜索词
		JsonNode wordJsonNode = jsonNode.get("word");
		BaiduOverviewReportVO wordRpt = parseJsonToBaiduOverviewReport(wordJsonNode,
				List.of("pv_count", "ratio"));
		// 入口页面
		JsonNode landingPageJsonNode = jsonNode.get("landingPage");
		BaiduOverviewReportVO landingPageRpt = parseJsonToBaiduOverviewReport(landingPageJsonNode,
				List.of("pv_count", "ratio"));
		// 访问页面
		JsonNode visitPageJsonNode = jsonNode.get("visitPage");
		BaiduOverviewReportVO visitPageRpt = parseJsonToBaiduOverviewReport(visitPageJsonNode,
				List.of("pv_count", "ratio"));
		// 新老用户
		JsonNode visitTypeJsonNode = jsonNode.get("visitType");
		BaiduOverviewReportVO visitTypeRpt = new BaiduOverviewReportVO();
		visitTypeRpt.setFields(
				List.of("pv_count", "visitor_count", "bounce_ratio", "avg_visit_time", "avg_visit_pages", "ratio"));
		visitTypeRpt.setXAxisDatas(List.of("newVisitor", "oldVisitor"));

		Map<String, List<Object>> visitTypeDatas = new HashMap<>();
		JsonNode newVisitor = visitTypeJsonNode.get("newVisitor");
		visitTypeDatas.put("newVisitor",
				List.of(newVisitor.get("pv_count").asInt(), newVisitor.get("visitor_count").asInt(),
						newVisitor.get("bounce_ratio").asDouble(), newVisitor.get("avg_visit_time").asInt(),
						newVisitor.get("avg_visit_pages").asDouble(), newVisitor.get("ratio").asDouble()));
		JsonNode oldVisitor = visitTypeJsonNode.get("oldVisitor");
		visitTypeDatas.put("oldVisitor",
				List.of(oldVisitor.get("pv_count").asInt(), oldVisitor.get("visitor_count").asInt(),
						oldVisitor.get("bounce_ratio").asDouble(), oldVisitor.get("avg_visit_time").asInt(),
						oldVisitor.get("avg_visit_pages").asDouble(), oldVisitor.get("ratio").asDouble()));
		visitTypeRpt.setDatas(visitTypeDatas);

		return Map.of("sourceSite", sourceSiteRpt, "word", wordRpt, "landingPage", landingPageRpt, "visitPage",
				visitPageRpt, "visitType", visitTypeRpt);
	}

	/**
	 * 通用结构的jsonNode解析
	 * 
	 * @param jsonNode
	 * @param metrics  响应结果实际指标
	 */
	private static BaiduOverviewReportVO parseJsonToBaiduOverviewReport(JsonNode jsonNode, List<String> metrics) {
		BaiduOverviewReportVO report = new BaiduOverviewReportVO();
		List<String> fields = new ArrayList<>();
		jsonNode.get("fields").forEach(node -> fields.add(node.asText()));
		report.setFields(fields);

		JsonNode itemsNode = jsonNode.get("items");

		List<String> xAxisDatas = new ArrayList<>();
		itemsNode.forEach(node -> xAxisDatas.add(node.get(0).asText()));

		Map<String, List<Object>> datas = fields.stream().filter(k -> metrics.contains(k))
				.collect(Collectors.toMap(k -> k, k -> new ArrayList<Object>(xAxisDatas.size())));
		itemsNode.forEach(node -> {
			for (int i = 1; i < fields.size(); i++) {
				String key = fields.get(i);
				List<Object> list = datas.get(key);
				if (list != null) {
					list.add(node.get(i).asText());
				}
			}
		});
		report.setXAxisDatas(xAxisDatas);
		report.setDatas(datas);
		return report;
	}

	public static List<Map<String, Object>> parseOverviewReportToTableData(BaiduOverviewReportVO report) {
		List<Map<String, Object>> list = new ArrayList<>(report.getXAxisDatas().size());
		List<String> xAxisDatas = report.getXAxisDatas();
		for (int i = 0; i < xAxisDatas.size(); i++) {
			String name = xAxisDatas.get(i);
			Map<String, Object> rowMap = new HashMap<>(3);
			rowMap.put("name", name);

			for (Entry<String, List<Object>> e : report.getDatas().entrySet()) {
				rowMap.put(e.getKey(), e.getValue().get(i));
			}
			list.add(rowMap);
		}
		return list;
	}
}
