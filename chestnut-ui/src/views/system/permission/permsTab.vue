<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('System.Permission.MenuPriv')" name="Menu">
        <permission-menu 
          v-if="this.activeName=='Menu'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-menu>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Permission.SitePriv')" name="Site">
        <permission-site 
          v-if="this.activeName=='Site'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-site>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Permission.CatalogPriv')" name="Catalog">
        <permission-catalog 
          v-if="this.activeName=='Catalog'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-catalog>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Permission.PageWidgetPriv')" name="PageWidget">
        <permission-pagewidget 
          v-if="this.activeName=='PageWidget'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-pagewidget>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import MenuPermission from './menuPerms';
import SitePermission from '@/views/cms/contentcore/sitePerms';
import CatalogPermission from '@/views/cms/contentcore/catalogPerms';
import PageWidgetPermission from '@/views/cms/contentcore/pageWidgetPerms';

export default {
  name: "PermisisonTab",
  components: {
    'permission-menu': MenuPermission,
    'permission-site': SitePermission,
    'permission-catalog': CatalogPermission,
    'permission-pagewidget': PageWidgetPermission
  },
  props: {
    ownerType: {
      type: String,
      default: ""
    },
    owner: {
      type: String,
      default: ""
    }
  },
  watch: {
    ownerType(newV, oldV) {
        this.permsOwnerType = newV;
    },
    owner(newV, oldV) {
      this.permsOwner = newV;
    }
  },
  data () {
    return {
      activeName: 'Menu',
      permsOwnerType: this.ownerType,
      permsOwner: this.owner
    };
  },
  methods: {
    handleTabClick (tab, event) {
    }
  }
};
</script>