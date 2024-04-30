<template>
  <div class="app-container adv-editor-container" v-loading="loading">
    <el-container>
      <el-header height="40px">
        <el-row :gutter="10" class="btn-row">
          <el-col :span="1.5">
            <el-button 
              plain
              type="info"
              icon="el-icon-back"
              size="mini"
              @click="handleGoBack">{{ $t('CMS.Adv.GoBack') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="success"
              icon="el-icon-edit"
              size="mini"
              v-hasPermi="[ $p('PageWidget:Edit:{0}', [ adSpaceId ]) ]"
              @click="handleSave">{{ $t("Common.Save") }}</el-button>
          </el-col>
        </el-row>
      </el-header>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="110px">
        <el-container>
          <el-aside style="width:500px;">
            <el-card shadow="never">
              <div slot="header" class="clearfix">
                <span>{{ $t('CMS.Adv.Basic') }}</span>
              </div>
              <el-form-item :label="$t('CMS.Adv.AdName')" prop="name">
                <el-input v-model="form.name" />
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.Type')" prop="type">
                <el-select v-model="form.type">
                  <el-option
                    v-for="t in adTypes"
                    :key="t.id"
                    :label="t.name"
                    :value="t.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.Weight')" prop="weight">
                <el-input-number v-model="form.weight" :min="0"></el-input-number>
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.OnlineDate')" prop="onlineDate">
                <el-date-picker v-model="form.onlineDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" />
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.OfflineDate')" prop="offlineDate">
                <el-date-picker v-model="form.offlineDate" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" />
              </el-form-item>
              <el-form-item :label="$t('Common.Remark')" prop="remark">
                <el-input v-model="form.remark" type="textarea" :maxlength="100" />
              </el-form-item>
            </el-card>
          </el-aside>
          <el-main>
            <el-card shadow="never">
              <div slot="header" class="clearfix; line-height: 32px; font-size: 16px;">
                <span>{{ $t('CMS.Adv.AdMaterials') }}</span>
              </div>
              <el-form-item label=""
                v-if="form.type==='image'"
                prop="resourcePath">
                <cms-logo-view v-model="form.resourcePath" :src="form.resourceSrc" :width="218" :height="150"></cms-logo-view>
              </el-form-item>
              <el-form-item
                :label="$t('CMS.Adv.RedirectUrl')"
                prop="redirectUrl">
                <el-input v-model="form.redirectUrl" placeholder="http(s)://" />
              </el-form-item>
            </el-card>
          </el-main>
        </el-container>
      </el-form>
    </el-container>
  </div>
</template>
<script>
import { listAdvertisementTypes, getAdvertisement, addAdvertisement, editAdvertisement } from "@/api/advertisement/advertisement";
import CMSLogoView from '@/views/cms/components/LogoView';

export default {
  name: "CMSAdvertisement",
  components: {
    'cms-logo-view': CMSLogoView
  },
  data () {
    return {
      loading: false,
      advertisementId: this.$route.query.id,
      adSpaceId: this.$route.query.adSpaceId,
      adTypes: [],
      form: {
        weight: 100
      },
      rules: {
        type: [
          { required: true, message: this.$t('CMS.Adv.RuleTips.Type'), trigger: "blur" }
        ],
        name: [
          { required: true, message: this.$t('CMS.Adv.RuleTips.Name'), trigger: "blur" }
        ],
        weight: [
          { required: true, message: this.$t('CMS.Adv.RuleTips.Weight'), trigger: "blur" },
        ],
        onlineDate: [
          { required: true, message: this.$t('CMS.Adv.RuleTips.OnlineDate'), trigger: "blur" }
        ],
        offlineDate: [
          { required: true, message: this.$t('CMS.Adv.RuleTips.OfflineDate'), trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.loadAdvertisementTypes();
    if (this.advertisementId) {
      this.loading = true;
      getAdvertisement(this.advertisementId).then(response => {
        this.form = response.data;
        this.loading = false;
      });
    } else {
      let onlineDate = new Date()
      onlineDate.setHours(0)
      onlineDate.setMinutes(0)
      onlineDate.setSeconds(0)
      this.$set(this.form, "onlineDate", this.parseTime(onlineDate))
      this.$set(this.form, "offlineDate", this.parseTime(new Date(2999, 11, 31)))
    }
  },
  methods: {
    loadAdvertisementTypes () {
      listAdvertisementTypes(this.queryParams).then(response => {
        this.adTypes = response.data.rows;
        if (!this.advertisementId && this.adTypes.length > 0) {
          this.form.type = this.adTypes[0].id;
        }
      });
    },
    handleGoBack() {
      const obj = { path: "/cms/adspace/editor", query: { id: this.adSpaceId } };
      this.$tab.closeOpenPage(obj);
    },
    handleSave() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.form.advertisementId) {
            editAdvertisement(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.loading = false;
            });
          } else {
            this.form.adSpaceId = this.adSpaceId;
            addAdvertisement(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: this.adSpaceId, id: response.data } });
              this.loading = false;
            });
          }
        }
      });
    }
  }
};
</script>
<style scoped>
.adv-editor-container .el-input, .el-textarea, .el-select, .el-input-number {
  width: 300px;
}
.adv-editor-container .el-header {
  padding-left: 0;
}
.adv-editor-container .el-aside, .el-main {
  padding: 0;
}
.adv-editor-container .el-aside {
  padding-right: 10px;
  background-color: #fff;
}
.adv-editor-container .el-aside, .el-container {
  line-height: 24px;
  font-size: 16px;
}
.adv-editor-container .el-card {
  height: 460px;
}
</style>