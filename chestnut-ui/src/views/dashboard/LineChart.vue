<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import echarts from 'echarts'
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
      this.chart = echarts.init(this.$el, 'macarons')
      this.setOptions(this.chartData)
    },
    setOptions({ xAxisDatas, datas } = {}) {
      var legendDatas = Object.keys(datas);
      var series = Object.keys(datas).map(key => {
          var arr = datas[key];
          return {
            name: key,
            smooth: false, // 平滑曲线
            type: 'line',
            // stack: 'Total',
            data: arr,
            animationDuration: 2800,
            animationEasing: 'quadraticOut',
          }
      });

      this.chart.setOption({
        xAxis: {
          type: 'category',
          data: xAxisDatas,
          boundaryGap: false
        },
        grid: {
          left: 20,
          right: 35,
          bottom: 20,
          top: 30,
          containLabel: true
        },
        toolbox: {
          feature: {
            // saveAsImage: {}
          }
        },
        tooltip: {
          show: true,
          trigger: 'axis',
          axisPointer: {
            type: 'line'
          }
        },
        yAxis: {
          type: 'value'
        },
        legend: {
          data: legendDatas
        },
        series: series
      })
    }
  }
}
</script>
