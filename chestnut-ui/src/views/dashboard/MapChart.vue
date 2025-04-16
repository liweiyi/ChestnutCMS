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
    setOptions({ dataList, min, max } = {}) {
      this.chart.setOption({
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
          show: true,
          formatter: function (params) {
            if (params.value.length > 1) {
              return params.name + "：" + params.value[2];
            } else {
              return params.name + "：" + (params.value || 0);
            }
          },
        },
        geo: {
          type: "map",
          zoom: 1.2,
          show: true,
          roam: false, // 是否允许缩放拖动
          map: "china", //使用 registerMap 注册的地图名称
          label: { show: false }, // 显示地区名称
          emphasis: {
            label: {
              show: false,
            },
          },
          layoutSize: "100%",
          itemStyle: {
            areaColor: '#efefef', // 默认背景色（无数据时的颜色）
            borderColor: "#666",
            borderWidth: 1,
            shadowColor: "rgba(10,76,139,1)",
            shadowOffsetY: 0,
            shadowBlur: 0, // 阴影大小
          },
        },
        series: [
          {
            name: 'china',
            type: 'map',
            aspectScale: 0.75,
            zoom: 1.2,
            geoIndex: 0,
            label: {
              normal: {
                show: true,
                color: "#4dccff",
              },
              emphasis: {
                show: false,
              },
            },
            itemStyle: {
              normal: {
                areaColor: {
                  x: 0,
                  y: 0,
                  x2: 1,
                  y2: 1,
                  colorStops: [
                    {
                      offset: 0,
                      color: "#A5DCF4", // 0% 处的颜色
                    },
                    {
                      offset: 1,
                      color: "#006EDD", // 100% 处的颜色
                    },
                  ],
                },
                borderColor: "#215495",
                borderWidth: 1,
              },
              emphasis: {
                areaColor: "#061E3D",
              },
            },
            data: dataList
          }
        ],
        visualMap: {
          type: "continuous",
          min: min,
          max: max == min ? max + 100 : max,
          show: true,
          seriesIndex: [0],  // 关联的数据系列索引
          inRange: {
            color: ["#A5DCF4", "#006edd"], // 颜色渐变范围（浅到深）
          },
          textStyle: { color: '#666' },
          left: 'right' // 位置调整
        },
      })
    }
  }
}
</script>
