<template>
  <div class="perms-tab-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('System.Permission.MenuPriv')" name="Menu">
        <permission-menu 
          v-if="activeName=='Menu'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-menu>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Permission.SitePriv')" name="Site">
        <permission-site 
          v-if="activeName=='Site'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-site>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Permission.CatalogPriv')" name="Catalog">
        <permission-catalog 
          v-if="activeName=='Catalog'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-catalog>
      </el-tab-pane>
      <el-tab-pane :label="$t('System.Permission.PageWidgetPriv')" name="PageWidget">
        <permission-pagewidget 
          v-if="activeName=='PageWidget'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-pagewidget>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="PermsTab">
import PermissionMenu from './menuPerms';
import PermissionSite from '@/views/cms/contentcore/sitePerms';
import PermissionCatalog from '@/views/cms/contentcore/catalogPerms';
import PermissionPagewidget from '@/views/cms/contentcore/pageWidgetPerms';

const props = defineProps({
  ownerType: {
    type: String,
    default: ""
  },
  owner: {
    type: String,
    default: ""
  }
});

watch(() => props.ownerType, (newV, oldV) => {
  if (newV && newV != '') {
    permsOwnerType.value = newV;
  }
});

watch(() => props.owner, (newV, oldV) => {
  if (newV && newV != '') {
    permsOwner.value = newV;
  }
});

const activeName = ref('Menu');
const permsOwnerType = ref(props.ownerType);
const permsOwner = ref(props.owner);

function handleTabClick(tab, event) {
  activeName.value = tab.name;
}
</script>