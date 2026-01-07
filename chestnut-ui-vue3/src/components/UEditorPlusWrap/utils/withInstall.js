import { camelize } from './camelize';

export function withInstall(options) {
  options.install = (app) => {
    app.component(options.name, options);
    app.component(camelize(`-${options.name}`), options);
  }
  return options
}