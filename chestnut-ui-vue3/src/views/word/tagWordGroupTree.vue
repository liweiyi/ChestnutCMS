<template>
  <div class="group-tree">
    <div v-if="showAdd" class="head-container">
      <el-button type="text" icon="Plus" @click="handleAdd">{{ $t("WordMgr.TAG.AddGroup") }}</el-button>
      <el-input
        :placeholder="$t('WordMgr.TAG.Placeholder.InputGroupName')"
        v-model="filterGroupName"
        clearable
        suffix-icon="Search"
      >
      </el-input>
    </div>
    <div class="tree-container">
      <el-button
        type="text"
        class="tree-header"
        icon="HomeFilled"
        @click="handleTreeRootClick"
        >{{ $t("APP.TITLE") }}</el-button
      >
      <el-tree
        :data="groupOptions"
        :props="defaultProps"
        :expand-on-click-node="false"
        :default-expanded-keys="treeExpandedKeys"
        :filter-node-method="filterNode"
        node-key="id"
        ref="treeRef"
        highlight-current
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <span class="node-text" :title="node.label">{{ node.label }}</span>
            <span class="node-tool">
              <el-button
                type="text"
                :title="$t('Common.Edit')"
                icon="Edit"
                @click="handleEdit(data)"
              >
              </el-button>
              <el-button
                type="text"
                :title="$t('Common.Delete')"
                icon="Delete"
                @click="handleDelete(data)"
              >
              </el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </div>
    <!-- 添加分组对话框 -->
    <el-dialog
      :title="diagTitle"
      v-model="diagOpen"
      width="600px"
      append-to-body
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('WordMgr.TAG.ParentGroup')" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="groupOptions"
            :placeholder="$t('WordMgr.TAG.Placeholder.SelectParentGroup')"
          />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.TAG.GroupName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.TAG.GroupCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item :label="$t('WordMgr.TAG.GroupLogo')" prop="logo">
          <cms-logo-view
            v-model="form.logo"
            :width="218"
            :height="150"
          ></cms-logo-view>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="TagWordGroupTree">
import { codeValidator } from "@/utils/validate";
import {
  getTagWordGroupTreeData,
  addTagWordGroup,
  editTagWordGroup,
  deleteTagWordGroup,
} from "@/api/word/tagWord";
import CmsLogoView from "@/views/cms/components/LogoView";

const props = defineProps({
  showAdd: {
    type: Boolean,
    default: true,
    required: false,
  },
});

const emit = defineEmits(["node-click"]);

const { proxy } = getCurrentInstance();

const diagOpen = ref(false);
// 分组树过滤：名称
const filterGroupName = ref(undefined);
// 分组树数据
const groupOptions = ref(undefined);
// 当前选中分组ID
const selectedGroupId = ref(undefined);
const treeExpandedKeys = ref([]);
const defaultProps = ref({
  children: "children",
  label: "label",
});
const diagTitle = ref("");
const objects = reactive({
  form: {}
});
const { form } = toRefs(objects);
const rules = {
  name: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
  ],
  code: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
    { validator: codeValidator, trigger: "change" },
  ],
};

watch(filterGroupName, (val) => {
  proxy.$refs.treeRef.filter(val);
});

onMounted(() => {
  loadGroupTreeData();
});

const loadGroupTreeData = () => {
  getTagWordGroupTreeData().then((response) => {
    groupOptions.value = response.data;
    nextTick(() => {
      selectedGroupId.value = proxy.$cache.local.get("LastSelectedGroupId");
      if (selectedGroupId.value && proxy.$refs.treeRef) {
        proxy.$refs.treeRef.setCurrentKey(selectedGroupId.value);
        treeExpandedKeys.value = [selectedGroupId.value];
        emit("node-click", proxy.$refs.treeRef.getCurrentNode());
      } else {
        emit("node-click", null);
      }
    });
  });
};
// 筛选节点
const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) > -1;
};
// 根节点点击事件
const handleTreeRootClick = () => {
  selectedGroupId.value = undefined;
  proxy.$cache.local.remove("LastSelectedGroupId");
  proxy.$refs.treeRef.setCurrentKey(null);
  emit("node-click", null);
};
// 节点单击事件
const handleNodeClick = (data) => {
  selectedGroupId.value = data.id;
  proxy.$cache.local.set("LastSelectedGroupId", selectedGroupId.value);
  emit("node-click", data);
};
// 取消按钮
const cancel = () => {
  diagOpen.value = false;
  reset();
};
// 表单重置
const reset = () => {
  proxy.resetForm("form");
};
/** 新增按钮操作 */
const handleAdd = () => {
  form.value = {};
  form.value.parentId = selectedGroupId.value;
  diagTitle.value = proxy.$t("WordMgr.TAG.AddGroupTitle");
  diagOpen.value = true;
};
const handleEdit = (data) => {
  form.value = {
    groupId: data.id,
    parentId: data.parentId == "0" ? undefined : data.parentId,
    name: data.label,
    code: data.props.code,
    logo: data.props.logo,
    logoSrc: data.props.logoSrc,
  };
  diagTitle.value = proxy.$t("WordMgr.TAG.EditGroupTitle");
  diagOpen.value = true;
};
/** 分组表单提交 */
const handleSave = () => {
  proxy.$refs.formRef.validate((valid) => {
    if (valid) {
      if (!form.value.parentId || form.value.parentId.length == 0) {
        form.value.parentId = 0;
      }
      if (form.value.groupId) {
        editTagWordGroup(form.value).then((response) => {
          diagOpen.value = false;
          proxy.$modal.msgSuccess(response.msg);
          loadGroupTreeData();
        });
      } else {
        addTagWordGroup(form.value).then((response) => {
          proxy.$cache.local.set("LastSelectedGroupId", response.data.groupId);
          diagOpen.value = false;
          proxy.$modal.msgSuccess(response.msg);
          loadGroupTreeData();
        });
      }
    }
  });
};
const handleDelete = (data) => {
  const groupIds = [data.id];
  proxy.$modal
    .confirm(proxy.$t("Common.ConfirmDelete"))
    .then(function () {
      return deleteTagWordGroup(groupIds);
    })
    .then((response) => {
      proxy.$modal.msgSuccess(response.msg);
      loadGroupTreeData();
    })
    .catch(() => {});
};
</script>
<style scoped>
.group-tree .tree-header {
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
  color: #424242;
  width: 100%;
  justify-content: flex-start;
  padding-left: 4px;
}
.group-tree-header:hover {
  background-color: #f5f7fa;
}
.group-tree .tree-node {
  width: 100%;
  line-height: 30px;
  position: relative;
  overflow: hidden;
}
.group-tree .tree-node .node-text {
  display: block;
  text-overflow: ellipsis;
  overflow: hidden;
}
.group-tree .tree-node .node-tool {
  display: none;
  position: absolute;
  right: 0;
  top: 0;
  background-color: #fff;
  padding: 0 5px;
  opacity: 0.8;
}
.group-tree .tree-node:hover .node-tool {
  display: inline-block;
}
.group-tree .tree-node .node-tool .el-button {
  font-size: 14px;
}
</style>
