<template>
  <div :class="className" ref="echartsMap" :style="{height:height,width:width}" />
</template>

<script>
import echarts from 'echarts'
import chinaJson from "echarts/map/json/china.json"
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.echartsMap)
      echarts.registerMap("china", chinaJson);
      this.setOptions(this.chartData)
    },
    setOptions({ xAxisDatas, datas } = {}) {
      this.chart.setOption({
        geo: {
          type: "map",
          map: "china", //使用 registerMap 注册的地图名称
          label: {
            normal: {
              show: false, //显示省份名称
            },
          },
        },
        title: {
          text: '访问来源'
        },
        toolbox: {
          show: true,
          //orient: 'vertical',
          left: 'right',
          top: 'top',
          feature: {
            dataView: { readOnly: false },
            saveAsImage: {}
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: function (params) {
            var value = (params.value + "").split(".");
            value =
              value[0].replace(/(\d{1,3})(?=(?:\d{3})+(?!\d))/g, "$1,") +
              "." +
              value[1];
            return params.seriesName + "<br/>" + params.name + " : " + value;
          },
          showDelay: 0,
          transitionDuration: 0.2
        },
        series: [
          {
            name: 'China',
            type: 'map',
            roam: true,
            itemStyle: {
              normal: {
                areaColor: '#6FA7CE', //地图颜色
                borderColor: '#fff', //地图边线颜色
              },
              emphasis: {
                label: {
                  show: true,
                  color: '#FFFFFF',
                  fontSize: 15,
                  fontWeight: 600
                },
                itemStyle: {
                  areaColor: 'rgba(102, 182, 255, 0.7)',
                  borderColor: '#FFFFFF',
                  borderWidth: 2
                }
              }
            },
            data: [
              { name: '湖南', value: 123 },
              { name: '湖北', value: 2354 },
              { name: '广东', value: 567 },
              { name: '北京', value: 8768 }
            ]
          }
        ]
      })
    }
  }
}
</script>
