import { pinYinLib } from './pinYinLib'

export default {
  fullSpelling: function (str) {
    return this.spelling(str, true);
  },
  firstSpelling: function (str) {
    return this.spelling(str, false);
  },
  spelling: function(str, full) {
    if (!str || str.length == 0) {
      return str;
    }
    const len = str.length;
    const reg = new RegExp('[a-zA-Z0-9_]');
    let spelling = '';
    for (let i = 0; i < len; i++) {
      const val = str.substring(i, i + 1);
      if (reg.test(val)) {
        spelling += val;
      } else {
        const name = this.arraySearch(val);
        if (name && name.length > 0) {
          if (full) {
            spelling = spelling + name.substring(0, 1).toUpperCase() + name.substring(1);
          } else {
            spelling += name.substring(0, 1);
          }
        } else {
          spelling += val;
        }
      }
    }
    return spelling.replace(/\s+/g, '_');
  },
  arraySearch: function (c) {
      for (let name in pinYinLib) {
          if (pinYinLib[name].indexOf(c) !== -1) {
            return name;
          }
      }
  }
}