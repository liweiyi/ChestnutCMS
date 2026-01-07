<template>
  <div class="video-player">
    <video ref="videoRef" class="video-js" />
  </div>
</template>
<script setup name="VideoPlayer">
import videojs from 'video.js';
import 'video.js/dist/video-js.css';

const { proxy } = getCurrentInstance();

const props = defineProps({
  options: {
    type: Object,
    default: () => ({}),
  },
});

const player = ref(null);

const defaultOptions = ref({
    language: 'zh-CN', // 语言
    playbackRates: [0.5, 1.0, 1.25, 1.5, 2.0, 3.0], // 播放速度
    autoplay: false, // 自动播放
    muted: false, // 静音
    loop: false, // 循环播放
    controls: true, // 是否显示控制条
    preload: 'auto', // 预加载
    autoplay: false, // 自动播放
    fluid: true, // 自适应宽高
});

watch(() => props.options, (newVal) => {
    if (newVal) {
        Object.assign(defaultOptions.value, newVal);
    }
    nextTick(() => {
        initVideoPlayer();
    });
}, { immediate: true });

function initVideoPlayer() {
    if (player.value) {
        player.value.src(defaultOptions.value.sources[0].src);
        player.value.poster(defaultOptions.value.poster);
        return;
    }
    console.log('video.defaultOptions', defaultOptions.value);
    player.value = videojs(proxy.$refs.videoRef, defaultOptions.value,  () => {
        player.value.log('video.js is ready!');
    });
}

function setSrc(srcUrl) {
    if (player.value) {
        player.value.src(srcUrl);
    }
}

function setPoster(posterUrl) {
    if (player.value) {
        player.value.poster(posterUrl);
    }
}

onBeforeUnmount(() => {
    if (player.value) {
        player.value.dispose();
    }
});

defineExpose({
    setSrc,
    setPoster,
});
</script>