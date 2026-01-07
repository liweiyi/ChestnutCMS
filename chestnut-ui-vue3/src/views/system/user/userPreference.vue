<template>
  <div class="app-container">
    <el-form ref="formRef" :model="form" label-width="240px">
      <el-form-item :label="$t('System.UserPreference.Shortcut')" prop="Shortcut">
        <el-tree-select
          ref="shortcutTreeSelect"
          v-model="form.Shortcut"
          :data="menuOptions"
          multiple
          :placeholder="$t('System.Menu.Placeholder.ParentMenu')"
          check-strictly
          show-checkbox
          :render-after-expand="false"
          node-key="id"
          :default-expanded-keys="form.Shortcut"
          :default-checked-keys="form.Shortcut"
        />
      </el-form-item>
      <el-form-item :label="$t('System.UserPreference.StatIndex')" prop="StatIndex">
        <el-tree-select
          ref="statIndexTreeSelect"
          v-model="form.StatIndex"
          :data="statMenuOptions"
          check-strictly
          :render-after-expand="false"
          node-key="id"
          :default-expanded-keys="statIndexExpandedKeys"
        />
      </el-form-item>
      <el-form-item :label="$t('System.UserPreference.ShowContentSubTitle')" prop="ShowContentSubTitle">
        <el-switch
          v-model="form.ShowContentSubTitle"
          :active-text="$t('Common.Yes')"
          :inactive-text="$t('Common.No')"
          active-value="Y"
          inactive-value="N">
        </el-switch>
      </el-form-item>
      <el-form-item :label="$t('System.UserPreference.IncludeChildContent')" prop="IncludeChildContent">
        <el-switch
          v-model="form.IncludeChildContent"
          :active-text="$t('Common.Yes')"
          :inactive-text="$t('Common.No')"
          active-value="Y"
          inactive-value="N">
        </el-switch>
      </el-form-item>
      <el-form-item :label="$t('System.UserPreference.OpenContentEditorW')" prop="OpenContentEditorW">
        <el-switch
          v-model="form.OpenContentEditorW"
          :active-text="$t('Common.Yes')"
          :inactive-text="$t('Common.No')"
          active-value="Y"
          inactive-value="N">
        </el-switch>
      </el-form-item>
      <el-form-item :label="$t('System.UserPreference.CatalogTreeExpandMode')" prop="CatalogTreeExpandMode">
        <el-select v-model="form.CatalogTreeExpandMode" clearable>
          <el-option :label="$t('System.UserPreference.CatalogTreeExpandMode_Accordion')" value="accordion"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="handleSave">{{ $t('Common.Save') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup name="UserPreference">
import { getUserMenuTree } from "@/api/system/menu";
import { getStatMenuTreeSelectorData } from "@/api/stat/stat"
import { getUserPreferences, saveUserPreferences } from "@/api/system/user";

const { proxy } = getCurrentInstance()

const menuOptions = ref([])
const statMenuOptions = ref([])
const data = reactive({
  form: {
    Shortcut: [], // 初始化 Shortcut 为空数组，确保多选组件正确初始化
    StatIndex: null // 初始化 StatIndex 为 null
  },
  statIndexExpandedKeys: [],
})

const treeSelectProps = {
  value: "id",
  label: "label",
  children: "children",
  disabled: "disabled",
}

const { form, statIndexExpandedKeys } = toRefs(data)

const loadMenuTreeselect = () => {
  getUserMenuTree().then(response => {
    disableDirectoryMenuNodes(response.data);
    menuOptions.value = [{ 
      id: '0', 
      parentId: '', 
      label: proxy.$t('APP.TITLE'), 
      isRoot: true,
      disabled: true,
      isDefaultExpanded: true, 
      children: response.data 
    }];
  });
}
function disableDirectoryMenuNodes(nodes) {
  nodes.forEach(node => {
    if (node.props.type === 'M') {
      node.disabled = true;
    }
    if (node.children && node.children.length > 0) {
      disableDirectoryMenuNodes(node.children);
    }
  });
}

const loadStatMenuTreeSelectorData = () => {
  getStatMenuTreeSelectorData().then(response => {
    statMenuOptions.value = response.data;
  })
}

const loadUserPreference = () => {
  getUserPreferences().then(response => {
    form.value = response.data;
    if (response.data.StatIndex) {
      statIndexExpandedKeys.value = [response.data.StatIndex];
    } else {
      statIndexExpandedKeys.value = [];
    }
    // proxy.$nextTick(() => {
    //   console.log(proxy.$refs.shortcutTreeSelect.getCheckedKeys());
    //   proxy.$refs.shortcutTreeSelect.setCheckedKeys(form.value.Shortcut, true);
    //   proxy.$refs.statIndexTreeSelect.setCheckedKeys(form.value.StatIndex, true);
    // });
  });
}

const handleSave = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      saveUserPreferences(form.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
      });
    }
  });
}

loadMenuTreeselect();
loadStatMenuTreeSelectorData();
loadUserPreference();
</script>
