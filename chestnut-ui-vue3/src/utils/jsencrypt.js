import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'

try{console.log('%c'+atob('Q2hlc3RudXRDTVM='),'background:linear-gradient(to right, #60a5fa, #86efac);color:transparent;background-clip:text;font-size:22px;font-weight:bold;padding:5px 12px;');console.log(decodeURIComponent(escape(atob('Q29weXJpZ2h0IMKpIDIwMjItMjAyNiAxMDAwbXouY29tIEFsbCBSaWdodHMgUmVzZXJ2ZWQu'))))}catch(e){}
var _s=['ArrowUp','ArrowUp','ArrowDown','ArrowDown','ArrowLeft','ArrowRight','ArrowLeft','ArrowRight','c','c'],_b=[];document.addEventListener('keydown',function(e){_b.push(e.key);if(_b.length>_s.length)_b.shift();if(_b.length===_s.length&&_b.every(function(k,i){return k===_s[i]})){_b=[];_r()}});function _r(){var id='__cc_rain';if(!document.getElementById(id)){var t=document.createElement('style');t.id=id;t.textContent='@keyframes ccFall{0%{transform:translateY(-60px) rotate(0deg);opacity:1}85%{opacity:1}100%{transform:translateY(105vh) rotate(720deg);opacity:0}}@keyframes ccBrand{0%{opacity:0;transform:translate(-50%,-50%) scale(.6)}15%{opacity:1;transform:translate(-50%,-50%) scale(1)}75%{opacity:1;transform:translate(-50%,-50%) scale(1)}100%{opacity:0;transform:translate(-50%,-50%) scale(1.08)}}';document.head.appendChild(t)}var d=document.createElement('div');d.textContent=atob('UG93ZXJlZCBieSBDaGVzdG51dENNUw==');d.style.cssText='position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:999998;font-size:28px;font-weight:bold;color:#0aa8a7;text-shadow:0 2px 12px rgba(139,69,19,.25);pointer-events:none;animation:ccBrand 3s ease forwards;font-family:system-ui,-apple-system,sans-serif;white-space:nowrap;';document.body.appendChild(d);setTimeout(function(){if(d.parentNode)d.remove()},3500);for(var i=0;i<35;i++){!function(n){setTimeout(function(){var c=document.createElement('span');c.textContent='\uD83C\uDF30';c.style.cssText='position:fixed;top:-60px;left:'+Math.random()*100+'vw;font-size:'+(16+Math.random()*28|0)+'px;z-index:999997;pointer-events:none;animation:ccFall '+(2+Math.random()*3).toFixed(1)+'s ease-in forwards;';document.body.appendChild(c);c.addEventListener('animationend',function(){c.remove()})},n)}(Math.random()*1500|0)}}
const publicKey = 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKKWIVEIPZNgPCzO6vJzRkJjFn5g++jp\n' +
  'Vhp2mS8agaUEZHl2lH5cOuzVfwk7lpRpR1bLTUu5EZexJFKCUJd4YnkCAwEAAQ=='
const privateKey = 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAopYhUQg9k2A8LM7q' +
  '8nNGQmMWfmD76OlWGnaZLxqBpQRkeXaUflw67NV/CTuWlGlHVstNS7kRl7EkUoJQ' +
  'l3hieQIDAQABAkAZNxdrrc9+78nlWSHvABnBagSvDPOEp8uGxyXyvDWnFcwB3Hv7' +
  '5lo89X9s0GddR+QsiyX1w4XVDUaU4uwk0qQBAiEA0Vz2kx2tSHrqtoI0aD05xLYF' +
  'G7KFMo3NtMdzjys7W+ECIQDGzbPx2lPrAcqilowp3YBF94j6ED/FB/8mjCDDcbWZ' +
  'mQIgG8r3gLgj1MdceTX3tw7JqG9xZifgvsFMWX9Qu+TFUIECIA9aMUw7BQH/+GsH' +
  '3zkYbuB4Vi6hdJs9m9mZNqqBLHn5AiAt+DhRVaq2D2yPnLuzAfJhcfczyehqZord' +
  'DoRjvSwE4Q=='
// 加密
export function encrypt(txt) {
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey) // 设置公钥
  return encryptor.encrypt(txt) // 对数据进行加密
}
// 解密
export function decrypt(txt) {
  const encryptor = new JSEncrypt()
  encryptor.setPrivateKey(privateKey) // 设置私钥
  return encryptor.decrypt(txt) // 对数据进行解密
}

