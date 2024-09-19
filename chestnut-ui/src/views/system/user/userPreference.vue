<template>
  <div class="app-container">
    <el-form ref="form" :model="form" label-width="240px">
      <el-form-item :label="$t('System.UserPreference.Shortcut')" prop="Shortcut">
        <treeselect
          v-model="form.Shortcut"
          :options="menuOptions"
          :multiple="true"
          :placeholder="$t('System.Menu.Placeholder.ParentMenu')"
        />
      </el-form-item>
      <el-form-item :label="$t('System.UserPreference.StatIndex')" prop="StatIndex">
        <treeselect
          v-model="form.StatIndex"
          :options="statMenuOptions"
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
        <el-button type="primary" size="smjall" @click="handleSave">{{ $t('Common.Save') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getUserMenuTree } from "@/api/system/menu";
import { getStatMenuTreeSelectorData } from "@/api/stat/stat"
import { getUserPreferences, saveUserPreferences } from "@/api/system/user";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Profile",
  components: { Treeselect },
  data() {
    return {
      menuOptions: [],
      statMenuOptions: [],
      form: {},
    };
  },
  created() {
    this.loadMenuTreeselect();
    this.loadStatMenuTreeSelectorData();
    this.loadUserPreference();
  },
  methods: {
    loadMenuTreeselect() {
      getUserMenuTree().then(response => {
        this.menuOptions = [{ id: '0', parentId: '', label: this.$t('APP.TITLE'), isRoot: true, isDefaultExpanded: true, children: response.data }];
      });
    },
    loadStatMenuTreeSelectorData() {
      getStatMenuTreeSelectorData().then(response => {
        this.statMenuOptions = response.data;
      })
    },
    loadUserPreference() {
      getUserPreferences().then(response => {
        this.form = response.data;
      });
    },
    handleSave() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          saveUserPreferences(this.form).then(response => {
            this.$modal.msgSuccess(response.msg);
          });
        }
      });
    }
  }
};
</script>
