<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="resource-dialog"
      :title="$t('CMS.UEditor.BaiduMap.DialogTitle')"
      v-model="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row>
        <el-form ref="formBdMap" :model="formData" inline class="el-form-search">
          <el-form-item :label="$t('CMS.UEditor.BaiduMap.City')" prop="city">
            <el-input v-model="formData.city"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">{{ $t("Common.Search") }}</el-button>
          </el-form-item>
        </el-form>
      </el-row>
      <el-row v-if="!ak||ak.length==0">
        <div style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
          <i class="el-icon-info mr5"></i>{{ $t("CMS.UEditor.BaiduMap.NoAccessKey") }}
          <el-button type="text" @click="handleGoSysConfig">{{ $t("CMS.UEditor.BaiduMap.GoSysConfig") }}</el-button>
        </div>
      </el-row>
      <el-row v-else>
        <baidu-map class="baidu-map-view" :ak="ak" :center="center" :zoom="zoom" :scroll-wheel-zoom="true" @ready="handleReady" @moveend="handleMoveend">
          <bm-scale anchor="BMAP_ANCHOR_BOTTOM_LEFT"></bm-scale>
          <bm-navigation anchor="BMAP_ANCHOR_TOP_LEFT"></bm-navigation>
          <bm-marker :position="position" :dragging="true" animation="BMAP_ANIMATION_BOUNCE"></bm-marker>
          <bm-context-menu>
            <bm-context-menu-item :callback="markerPosition" :text="$t('CMS.UEditor.BaiduMap.MarkerPosition')"></bm-context-menu-item>
          </bm-context-menu>
        </baidu-map>
      </el-row>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSUeditorBaiduMap">
import { BaiduMap, BmScale, BmNavigation, BmMarker, BmLabel, BmContextMenu, BmContextMenuItem } from 'vue-baidu-map-3x'

const { proxy } = getCurrentInstance();

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  }
})
const emit = defineEmits(['update:open', 'ok', 'close'])

const visible = ref(props.open);
const ak = ref("")
const zoom = ref(0)
const width = ref(760)
const height = ref(300)

const objects = reactive({
  center: { lng: 0, lat: 0 },
  position: {lng: 116.404, lat: 39.915},
  formData: {},
})

const { center, position, formData } = toRefs(objects)

watch(() => props.open, (newVal) => {
  console.log('props.open', newVal);
  visible.value = newVal;
})
watch(visible, (newVal) => {
  if (!newVal) {
    noticeClose();
  }
})

onMounted(() => {
  proxy.getConfigKey("BaiduMapAccessKey").then(response => {
    ak.value = response.data;
  });
})
const handleReady = (bMap, map) => {
  center.value = {lng: 116.404, lat: 39.915};
  zoom.value = 12;
}
const handleMoveend = (e) => {
  syncCenterAndZoom(e);
}
const handleZoomend = (e) => {
  syncCenterAndZoom(e);
}
const syncCenterAndZoom = (e) => {
  if (e) {
    center.value = e.target.getCenter();
    zoom.value = e.target.getZoom();
    position.value = e.target.getCenter();
  }
}

const handleSearch = () => {
  center.value = formData.value.city;
}
const markerPosition = (e) => {
  position.value = { lng: e.point.lng, lat: e.point.lat };
}
const handleGoSysConfig = () => {
  proxy.$router.push({
    path: "/system/config"
  });
}
const handleOk = () => {
  noticeOk();
}
const handleCancel = () => {
  visible.value = false;
}
const noticeOk = () => {
  if (visible.value) {
    var url = "https://api.map.baidu.com/staticimage?center=" + center.value.lng + ',' + center.value.lat +
                "&zoom=" + zoom.value + "&width=" + width.value + "&height=" + height.value + "&markers=" + position.value.lng + ',' + position.value.lat;
    emit("ok", { src: url, width: width.value, height: height.value });
    visible.value = false;
  }
}
const noticeClose = () => {
  if (!visible.value) {
    emit("update:open", false);
    emit("close");
    reset();
  }
}
const reset = () => {
  formData.value = {
    city: ''
  }
}
</script>
<style scoped>
#container {
  overflow: hidden;
  width: 100%;
  height: 400px;
  margin: 0;
  font-family: "微软雅黑";
}
.baidu-map-view {
  width: 100%;
  height: 300px;
}
</style>