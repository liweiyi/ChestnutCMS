<template>
  <div ref="chartRef" :class="className" :style="{height:height,width:width}" />
</template>

<script setup name="LineChartComponent">
import * as echarts from 'echarts'
import 'echarts/theme/macarons'  // echarts theme
import useResize from './mixins/resize'

const props = defineProps({
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
})

const chartRef = ref(null)
const chart = ref(null)

// 使用 resize composable
useResize(chart)

watch(() => props.chartData, (val) => {
  setOptions(val)
}, { deep: true })

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

onBeforeUnmount(() => {
  if (!chart.value) {
    return
  }
  chart.value.dispose()
  chart.value = null
})

function initChart() {
  chart.value = echarts.init(chartRef.value, 'macarons')
  setOptions(props.chartData)
}

function setOptions({ xAxisDatas, datas } = {}) {
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

  chart.value.setOption({
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
</script>
