

/**
 * 通用js方法封装处理
 * Copyright (c) 2023 chestnut
 */

// 日期格式化
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(new RegExp(/-/gm), '/').replace('T', ' ').replace(new RegExp(/\.[\d]{3}/gm), '');
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') { return ['日', '一', '二', '三', '四', '五', '六'][value] }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

// 毫秒数转时长显示
export function transferDuration (milliseconds) {
  const totalSeconds = milliseconds / 1000;
  // hours
  const hours = totalSeconds / 3600;
  const hoursRound = Math.floor(hours);
  const hoursStr = `${hoursRound > 0 ? `${hoursRound}时` : ''}`;
  // minutes
  const minutes = totalSeconds % 3600 / 60;
  const minutesRound = Math.floor(minutes);
  const minutesStr = `${minutesRound > 0 ? `${minutesRound}分` : ''}`;
  // seconds
  const seconds = totalSeconds % 3600 % 60;
  const secondsRound = Math.floor(seconds)
  const secondsStr = `${secondsRound > 0 ? `${secondsRound}秒` : ''}`

  const str = `${hoursStr}${minutesStr}${secondsStr}`
  return str
}

// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  let search = params;
  search.params = typeof (search.params) === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {};
  dateRange = Array.isArray(dateRange) ? dateRange : [];
  if (typeof (propName) === 'undefined') {
    search.params['beginTime'] = dateRange[0];
    search.params['endTime'] = dateRange[1];
  } else {
    search.params['begin' + propName] = dateRange[0];
    search.params['end' + propName] = dateRange[1];
  }
  return search;
}

export function getTodayDateRange() {
	let date = new Date();
	let year = date.getFullYear();
	let month = date.getMonth();
	let day = date.getDate();
	return [ new Date(year, month, day, 0, 0, 0), new Date(year, month, day, 23, 59, 59) ];
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) {
    return "";
  }
  var actions = [];
  Object.keys(datas).some((key) => {
    if (datas[key].value == ('' + value)) {
      actions.push(datas[key].label);
      return true;
    }
  })
  if (actions.length === 0) {
    actions.push(value);
  }
  return actions.join('');
}

// 回显数据字典（字符串、数组）
export function selectDictLabels(datas, value, separator) {
  if (value === undefined || value.length ===0) {
    return "";
  }
  if (Array.isArray(value)) {
    value = value.join(",");
  }
  var actions = [];
  var currentSeparator = undefined === separator ? "," : separator;
  var temp = value.split(currentSeparator);
  Object.keys(value.split(currentSeparator)).some((val) => {
    var match = false;
    Object.keys(datas).some((key) => {
      if (datas[key].value == ('' + temp[val])) {
        actions.push(datas[key].label + currentSeparator);
        match = true;
      }
    })
    if (!match) {
      actions.push(temp[val] + currentSeparator);
    }
  })
  return actions.join('').substring(0, actions.join('').length - 1);
}

// 字符串格式化(%s )
export function sprintf(str) {
  var args = arguments, flag = true, i = 1;
  str = str.replace(/%s/g, function () {
    var arg = args[i++];
    if (typeof arg === 'undefined') {
      flag = false;
      return '';
    }
    return arg;
  });
  return flag ? str : '';
}

// 转换字符串，undefined,null等转化为""
export function parseStrEmpty(str) {
  if (!str || str == "undefined" || str == "null") {
    return "";
  }
  return str;
}

// 数据合并
export function mergeRecursive(source, target) {
  for (var p in target) {
    try {
      if (target[p].constructor == Object) {
        source[p] = mergeRecursive(source[p], target[p]);
      } else {
        source[p] = target[p];
      }
    } catch (e) {
      source[p] = target[p];
    }
  }
  return source;
};

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} rootId 根节点值
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
 export function handleTree(data, rootId, id, parentId, children) {
  let config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children'
  };

  var childrenListMap = {};
  var nodeIds = {};
  var tree = [];

  for (let d of data) {
    let parentId = d[config.parentId];
    if (childrenListMap[parentId] == undefined) {
      childrenListMap[parentId] = [];
    }
    nodeIds[d[config.id]] = d;
    childrenListMap[parentId].push(d);
  }

  for (let d of data) {
    d[config.childrenList] = childrenListMap[d[config.id]];
    if (d[config.parentId] == rootId) {
      tree.push(d);
    }
  }
  return tree;
}

/**
* 参数处理
* @param {*} params  参数
*/
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    var part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof (value[key]) !== 'undefined') {
            let params = propName + '[' + key + ']';
            var subPart = encodeURIComponent(params) + "=";
            result += subPart + encodeURIComponent(value[key]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result
}

// 验证是否为blob格式
export async function blobValidate(data) {
  try {
    const text = await data.text();
    JSON.parse(text);
    return false;
  } catch (error) {
    return true;
  }
}

export function isImage(filename) {
  if (!filename || filename.indexOf('.') < 0) {
    return false;
  }
  let suffix = filename.substring(filename.lastIndexOf('.'));
  return ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.tiff', '.svg'].indexOf(suffix) > -1
}

export function getFileSvgIconClass(path) {
  if (path && path.indexOf(".") > -1) {
    const suffix = path.substring(path.lastIndexOf('.'));
    switch(suffix) {
      case ".7z": return "file-7z";
      case ".aac": return "file-aac";
      case ".ai": return "file-ai";
      case ".archive": return "file-archive";
      case ".arj": return "file-arj";
      case ".avi": return "file-avi";
      case ".css": return "file-css";
      case ".csv": return "file-csv";
      case ".dbf": return "file-dbf";
      case ".doc": return "file-doc";
      case ".docx": return "file-doc";
      case ".dwg": return "file-dwg";
      case ".exe": return "file-exe";
      case ".fla": return "file-fla";
      case ".flac": return "file-flac";
      case ".gif": return "file-gif";
      case ".html": return "file-html";
      case ".iso": return "file-iso";
      case ".jpg": return "file-jpg";
      case ".js": return "file-js";
      case ".json": return "file-json";
      case ".mdf": return "file-mdf";
      case ".mp2": return "file-mp2";
      case ".mp3": return "file-mp3";
      case ".mp4": return "file-mp4";
      case ".mxf": return "file-mxf";
      case ".nrg": return "file-nrg";
      case ".pdf": return "file-pdf";
      case ".png": return "file-png";
      case ".ppt": return "file-ppt";
      case ".pptx": return "file-ppt";
      case ".psd": return "file-psd";
      case ".rar": return "file-rar";
      case ".rtf": return "file-rtf";
      case ".svg": return "file-svg";
      case ".text": return "file-text";
      case ".tiff": return "file-tiff";
      case ".txt": return "file-txt";
      case ".wav": return "file-wav";
      case ".wma": return "file-wma";
      case ".xls": return "file-xls";
      case ".xlsx": return "file-xls";
      case ".xml": return "file-xml";
      case ".zip": return "file-zip";
    }
    if (suffix == ".ogg" || suffix == ".aiff" || suffix == ".mid") {
      return "file-audio";
    }
    if (suffix == ".mpg" || suffix == ".mpeg" || suffix == ".rmvb" || suffix == ".rm" || suffix == ".wmv" || suffix == ".mov") {
      return "file-video";
    }
  }
  return "file-unknown";
}
