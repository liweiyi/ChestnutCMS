# UPNG quantization core provenance

- Upstream: Photopea UPNG.js, npm `upng-js` **2.1.0**.
- Exact source commit: `5e5af183a3dda1e320b90a1830b3f64bc92010d4`.
- Source: https://github.com/photopea/UPNG.js/blob/5e5af183a3dda1e320b90a1830b3f64bc92010d4/UPNG.js
- Published package: https://registry.npmjs.org/upng-js/-/upng-js-2.1.0.tgz
- npm tarball SHA-1: `7176e73973db361ca95d0fa14f958385db6b9dd2`.
- npm tarball integrity: `sha512-d3xzZzpMP64YkjP5pr8gNyvBt7dLk/uGI67EctzDuVp4lCZyVMo0aJO6l/VDlgbInJYDY6cnClLoBp29eKWI6g==`.
- License: MIT, Copyright (c) 2017 Photopea; complete text in `UPNG-LICENSE.txt`.

Only `UPNG.quantize`, its statistics/KD-tree/vector helpers, and `alphaMul`
are included. The application adapter only consumes the generated palette.
No PNG/APNG encoder, decoder, inflation/deflation implementation or pako is
included. This is the npm 2.1.0 API, not the newer GitHub master API.

Local modifications:

1. Start covariance power iteration from the axis with the largest diagonal
   variance instead of `[0.5, 0.5, 0.5, 0.5]`, avoiding the red/green and
   red/blue orthogonal-seed collapse. Ties select the first axis.
2. Stop iteration on zero or non-finite vector norm and retain a finite seed.
3. Reject empty/non-progressing pixel splits and non-finite split variance.
4. Expose a small ESM palette-only entry point with input validation.

Sampling, exact-color preservation, binary GIF transparency, full-image
palette mapping, bounds and caching are implemented by the application's
`quantize.js` adapter. No perceptual quality score or target search is claimed.
