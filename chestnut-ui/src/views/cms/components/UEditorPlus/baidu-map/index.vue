<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="resource-dialog"
      :title="$t('CMS.UEditor.BaiduMap.DialogTitle')"
      :visible.sync="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row>
        <el-form ref="formBdMap" :model="formData" inline>
          <el-form-item :label="$t('CMS.UEditor.BaiduMap.City')" prop="city">
            <el-input v-model="formData.city"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">{{ $t("Common.Search") }}</el-button>
          </el-form-item>
        </el-form>
      </el-row>
      <el-row v-if="!this.ak||this.ak.length==0">
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
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import BaiduMap from 'vue-baidu-map/components/map/Map.vue'
import BmScale from 'vue-baidu-map/components/controls/Scale.vue'
import BmNavigation from 'vue-baidu-map/components/controls/Navigation.vue'
import BmMarker from 'vue-baidu-map/components/overlays/Marker.vue'
import BmLabel from 'vue-baidu-map/components/overlays/Label.vue'
import BmContextMenu from 'vue-baidu-map/components/context-menu/Menu.vue'
import BmContextMenuItem from 'vue-baidu-map/components/context-menu/Item.vue'


export default {
  name: "CMSUeditorBaiduMap",
  components: {
    BaiduMap,
    BmScale,
    BmNavigation,
    BmMarker,
    BmLabel,
    BmContextMenu,
    BmContextMenuItem
  },
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    }
  },
  data () {
    return {
      visible: false,
      map: undefined,
      ak: "",
      center: { lng: 0, lat: 0 },
      position: {lng: 116.404, lat: 39.915},
      positionTitle: "",
      zoom: 0,
      formData: {
      },
      width: 760,
      height: 300
    };
  },
  watch: {
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.noticeClose();
      }
    }
  },
  created() {
    this.getConfigKey("BaiduMapAccessKey").then(response => {
      this.ak = response.data;
    });
  },
  methods: {
    handleReady(bMap, map) {
      this.center = {lng: 116.404, lat: 39.915};
      this.zoom = 12;
    },
    handleMoveend(e) {
      this.syncCenterAndZoom(e);
    },
    handleZoomend(e) {
      this.syncCenterAndZoom(e);
    },
    syncCenterAndZoom(e) {
      if (e) {
        this.center = e.target.getCenter();
        this.zoom = e.target.getZoom();
      }
    },
    handleSearch () {
      this.center = this.formData.city;
      this.position = this.formData.city;
    },
    markerPosition (e) {
      this.position = { lng: e.point.lng, lat: e.point.lat };
    },
    handleGoSysConfig () {
      this.$router.push({
        path: "/system/config"
      });
    },
    handleOk () {
      this.noticeOk();
    },
    handleCancel () {
      this.visible = false;
    },
    noticeOk () {
      if (this.visible) {
        var url = "https://api.map.baidu.com/staticimage?center=" + this.center.lng + ',' + this.center.lat +
                    "&zoom=" + this.zoom + "&width=" + this.width + "&height=" + this.height + "&markers=" + this.position.lng + ',' + this.position.lat;
        this.$emit("ok", { src: url, width: this.width, height: this.height });
        this.visible = false;
      }
    },
    noticeClose () {
      if (!this.visible) {
        this.$emit('update:open', false);
        this.$emit("close");
        this.reset();
      }
    },
    reset() {
      this.formData = {
      }
    }
  }
};
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