/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.media.util;

import com.chestnut.common.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import ws.schild.jave.*;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.filtergraphs.OverlayWatermark;
import ws.schild.jave.filters.helpers.OverlayLocation;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

@Slf4j
public class MediaUtils {

	/**
	 * 获取音视频文件信息
	 *
	 * @param url 文件路径
	 * @return 文件信息
	 */
	public static MultimediaInfo getMultimediaInfo(String url) {
		try {
			MultimediaObject multimediaObject;
			if (ServletUtils.isHttpUrl(url)) {
				multimediaObject = new MultimediaObject(new URL(url));
			} else {
				multimediaObject = new MultimediaObject(new File(url));
			}
			return multimediaObject.getInfo();
		} catch (EncoderException | MalformedURLException e) {
			log.error("Load media info failed: {}", url, e);
		}
		return null;
	}

	/**
	 * 视频加图片水印
	 *
	 * jave2插件 3.0版本后 针对windows版本的路径有bug,导致windows下添加水印命令不成功
	 * 但是3.0版本前，没找到添加水印功能，所以不考虑windows或则反编译MovieFilter.escapingPath 方法修改
	 * <p>
	 * movie='C\\:\\Users\\lin\\Desktop\\usedMarketIcon.png'[watermark];[0:v][watermark]overlay='main_w-overlay_w-10:main_h-overlay_h-10'
	 * <p>
	 * C\\: 其实只需要一个\
	 *
	 * @param source    源视频
	 * @param dest      水印视频
	 * @param watermark 水印图
	 * @param position  水印位置
	 * @throws EncoderException
	 * @throws FileNotFoundException
	 */
	public static void generateVideoImageWatermark(File source, File dest, File watermark, OverlayLocation position)
			throws EncoderException, FileNotFoundException {
		if (!source.exists()) {
			throw new FileNotFoundException(source.getAbsolutePath());
		}
		if (!watermark.exists()) {
			throw new FileNotFoundException(watermark.getAbsolutePath());
		}
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		VideoAttributes vidAttr = new VideoAttributes();
		OverlayWatermark overlayWatermark = new OverlayWatermark(watermark, position, -10, -10);
		log.info("overlayWatermark.getExpression()" + overlayWatermark.getExpression());
		vidAttr.addFilter(overlayWatermark);
		AudioAttributes audioAttributes = new AudioAttributes();
		audioAttributes.setCodec(AudioAttributes.DIRECT_STREAM_COPY);
		EncodingAttributes encAttr = new EncodingAttributes().setVideoAttributes(vidAttr)
				.setAudioAttributes(audioAttributes);
		new Encoder().encode(new MultimediaObject(source), dest, encAttr);
		stopWatch.stop();
		log.info("生成视频图片水印 =" + source + "水印视频 =" + dest + "耗时=" + stopWatch.getTime() + "毫秒");
	}

	/**
	 * 视频压缩
	 *
	 * @param source
	 * @param dest
	 * @param supplierAudio
	 * @param supplierVideo
	 * @throws IllegalArgumentException
	 * @throws InputFormatException
	 * @throws EncoderException
	 * @throws FileNotFoundException
	 */
	public static void compress(File source, File dest, Supplier<AudioAttributes> supplierAudio,
								Supplier<VideoAttributes> supplierVideo)
			throws IllegalArgumentException, InputFormatException, EncoderException, FileNotFoundException {
		if (!source.exists()) {
			throw new FileNotFoundException(source.getAbsolutePath());
		}
		long time = System.currentTimeMillis();

		MultimediaObject multimediaObject = new MultimediaObject(source);
		// 音频属性
		AudioAttributes audio = supplierAudio.get();
//		audio.setBitRate(audioInfo.getBitRate()); // 比特率
//		audio.setChannels(audioInfo.getChannles()); // 声道数
//		audio.setCodec(audioInfo.getDecoder()); // 编码格式 
//		audio.setSamplingRate(audioInfo.getSamplingRate()); // 采样率，越高声音的还原度越好，文件越大
//		audio.setVolume(256); // 音量值，未设置为0,如果256，则音量值不会改变

		// 视频属性
		VideoAttributes video = supplierVideo.get();
//		video.setCodec("h264"); // 编码方式
//		video.setBitRate(videoInfo.getBitRate()); // 比特率
//		video.setFrameRate(videoInfo.getFrameRate()); // 帧率
//		video.setSize(videoInfo.getSize()); // 视频分辨率设置

		EncodingAttributes attr = new EncodingAttributes();
		attr.setAudioAttributes(audio);
		attr.setVideoAttributes(video);
		// attr.setOutputFormat("mp4");
		// attr.setEncodingThreads(Runtime.getRuntime().availableProcessors() / 2);
		new Encoder().encode(multimediaObject, dest, attr);
		log.info("压缩视频={},新视频={},耗时={}秒", source.getAbsolutePath(), dest.getAbsolutePath(),
				(System.currentTimeMillis() - time) / 1000);
	}

	/**
	 * 视频截取
	 *
	 * @param source    原视频文件
	 * @param output    截图存储文件
	 * @param timestamp 截取的时间戳，单位：秒
	 * @throws EncoderException
	 * @throws FileNotFoundException
	 */
	public static void generateVideoScreenshot(File source, File output, long timestamp)
			throws FileNotFoundException, EncoderException {
		if (!source.exists()) {
			throw new FileNotFoundException(source.getAbsolutePath());
		}
		if (timestamp <= 0) {
			timestamp = 1;
		}
		MultimediaObject multimediaObject = new MultimediaObject(source);
		ScreenExtractor screenExtractor = new ScreenExtractor();
		int width = -1;
		int height = -1;
		long millis = timestamp * 1000;
		int quality = 1;
		screenExtractor.renderOneImage(multimediaObject, width, height, millis, output, quality);
		log.info("原视频 = {},生成截图 = {}", source.getAbsolutePath(), output.getAbsolutePath());
	}

	public static void generateRemoteVideoScreenshot(URL url, File output, long timestamp)
			throws EncoderException {
		if (timestamp <= 0) {
			timestamp = 1;
		}
		MultimediaObject multimediaObject = new MultimediaObject(url, true);
		ScreenExtractor screenExtractor = new ScreenExtractor();
		int width = -1;
		int height = -1;
		long millis = timestamp * 1000;
		int quality = 1;
		screenExtractor.renderOneImage(multimediaObject, width, height, millis, output, quality);
		log.info("原视频 = {},生成截图 = {}", url.getPath(), output.getAbsolutePath());
	}

	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		try {
			generateVideoScreenshot(new File("C:/Users/liwy/Videos/911Mothers_2010W-480p.mp4"),
					new File("C:/Users/liwy/Videos/screenshot.gif"), 78);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println((System.currentTimeMillis() - s) + " ms");
//		MultimediaInfo multimediaInfo = getMultimediaInfo("C:/Users/liwy/Videos/911Mothers_2010W-480p.mp4");
//		MultimediaInfo multimediaInfo = getMultimediaInfo("http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4");
//		// 封装格式
//		System.out.println(multimediaInfo.getFormat());
//		// 时长
//		System.out.println(multimediaInfo.getDuration());
//		// 元数据
//		System.out.println(multimediaInfo.getMetadata());
//		VideoInfo video = multimediaInfo.getVideo();
//		// 比特率：bps
//		System.out.println("BitRate: " + video.getBitRate());
//		// 视频编码方式
//		System.out.println("Decoder: " + video.getDecoder());
//		// 帧率：Hz
//		System.out.println("FrameRate: " + video.getFrameRate());
//		// 视频尺寸
//		System.out.println("width: " + video.getSize().getWidth());
//		System.out.println("height: " + video.getSize().getHeight());
//		System.out.println("EncoderArgument: " + video.getSize().asEncoderArgument());
//		System.out.println("aspectRatioExpression: " + video.getSize().aspectRatioExpression());
//		// VideoInfo元数据
//		System.out.println(video.getMetadata());
//		System.out.println("cost: " + (System.currentTimeMillis() - s));
//
//		s = System.currentTimeMillis();
//		MultimediaInfo multimediaInfo2 = getMultimediaInfo("C:/Users/liwy/Music/11582.mp3");
//		// 格式
//		System.out.println(multimediaInfo2.getFormat());
//		// 时长
//		System.out.println(multimediaInfo2.getDuration());
//		// 元数据
//		System.out.println(multimediaInfo2.getMetadata());
//		AudioInfo audio = multimediaInfo2.getAudio();
//		// 比特率：bps
//		System.out.println("BitRate: " + audio.getBitRate());
//		// 视频编码方式
//		System.out.println("Decoder: " + audio.getDecoder());
//		// 声道数
//		System.out.println("Channels: " + audio.getChannels());
//		// 取样率 Hz
//		System.out.println("SamplingRate: " + audio.getSamplingRate());
//		// AudioInfo元数据
//		System.out.println(audio.getMetadata());
//		System.out.println("cost: " + (System.currentTimeMillis() - s));
//
//		s = System.currentTimeMillis();
//
//		System.out.println("cost: " + (System.currentTimeMillis() - s));
	}
}
