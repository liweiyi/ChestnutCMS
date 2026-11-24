import test from 'node:test'
import assert from 'node:assert/strict'
import { isSubscriptionSeconds, normalizeSubscriptionSeconds, isSubscriptionMemberId, isSubscriptionValid, formatSubscriptionTime, createSubscriptionRequestId, buildSubscriptionAdjustment, buildSubscriptionRecordQuery, buildSubscriptionDefinition } from './formValues.js'

test('秒数增加上限跟随新接口，负数和会员ID仍保留Long精度', () => {
  for (const value of ['0', '+00060', '-60', '316224000', '-9223372036854775808']) assert.equal(isSubscriptionSeconds(value), true)
  for (const value of ['316224001', '9007199254740993', '9223372036854775807']) assert.equal(isSubscriptionSeconds(value), false)
  assert.equal(normalizeSubscriptionSeconds('+00060'), '60')
  assert.equal(normalizeSubscriptionSeconds('-00060'), '-60')
  assert.equal(normalizeSubscriptionSeconds('-0000'), '0')
})

test('超界、小数、科学计数以及空白不能作为整数秒数', () => {
  for (const value of ['9223372036854775808', '-9223372036854775809', '1.5', '1e3', '--1', ' 1', '', null, 10]) assert.equal(isSubscriptionSeconds(value), false)
})

test('时长上限不会错误限制会员ID，会员ID全程使用十进制字符串', () => {
  assert.equal(isSubscriptionMemberId('9007199254740993'), true)
  assert.equal(isSubscriptionMemberId('9223372036854775807'), true)
  for (const value of ['0', '-1', '+1', '1.5', '9223372036854775808']) assert.equal(isSubscriptionMemberId(value), false)
  assert.match(createSubscriptionRequestId(), /^[a-f0-9]{32}$/)
})

test('调整提交定义ID和新接口字段，不透传旧key、版本或客户端来源', () => {
  const payload = buildSubscriptionAdjustment({ memberId: '9007199254740993', subscriptionId: 'definition-uuid', subscriptionKey: 'old-key', deltaSeconds: '+00060', reason: '  调整说明  ', version: 9, sourceType: 'forged', sourceId: 'forged' }, 'request-1')
  assert.deepEqual(payload, { requestId: 'request-1', memberId: '9007199254740993', subscriptionId: 'definition-uuid', deltaSeconds: '60', reason: '调整说明' })
  assert.throws(() => buildSubscriptionAdjustment({ memberId: '1', subscriptionId: '', deltaSeconds: '60', reason: '原因' }, 'r'))
})

test('会员列表筛选提交ID和Y/N，清空时不提交旧字段或布尔值', () => {
  assert.deepEqual(buildSubscriptionRecordQuery({ pageNum: 2, pageSize: 20, memberId: '001', subscriptionId: 'definition-uuid', valid: 'Y', subscriptionKey: 'old' }), { pageNum: 2, pageSize: 20, memberId: '1', subscriptionId: 'definition-uuid', valid: 'Y' })
  assert.equal(buildSubscriptionRecordQuery({ valid: 'N' }).valid, 'N')
  const empty = buildSubscriptionRecordQuery({ memberId: '', subscriptionId: '', valid: false })
  assert.equal(empty.valid, undefined)
  assert.equal(empty.memberId, undefined)
  assert.equal(empty.subscriptionId, undefined)
})

test('定义编辑使用定义ID并保留服务项和备注，不再提交旧key或并发版本', () => {
  const form = { subscriptionId: 'uuid', subscriptionKey: 'common', subscriptionName: '普通订阅', serviceIds: ['download'], description: '说明', remark: '备注', version: 5, status: '1', subscribedCount: '99' }
  const edit = buildSubscriptionDefinition(form, true)
  assert.deepEqual(edit, { subscriptionId: 'uuid', subscriptionName: '普通订阅', description: '说明', serviceIds: ['download'], remark: '备注' })
  edit.serviceIds.push('other')
  assert.deepEqual(form.serviceIds, ['download'])
  assert.equal('subscriptionId' in buildSubscriptionDefinition(form, false), false)
})

test('秒级结束时间与浏览器毫秒时间比较，相等或已过期均无效', () => {
  assert.equal(isSubscriptionValid('1001', 1000000), true)
  assert.equal(isSubscriptionValid('1000', 1000000), false)
  assert.equal(isSubscriptionValid('999', 1000000), false)
  assert.equal(isSubscriptionValid('1001', 1000999), true)
  assert.equal(isSubscriptionValid('1001', 1001000), false)
  assert.equal(isSubscriptionValid(null, 1000000), false)
  assert.equal(isSubscriptionValid('invalid', 1000000), false)
  assert.equal(isSubscriptionValid('9007199254740993', '9007199254740992000'), true)
  assert.equal(isSubscriptionValid('9007199254740993', '9007199254740993000'), false)
})

test('时间展示区分到期秒时间戳和操作毫秒时间戳', () => {
  assert.equal(formatSubscriptionTime(null), '—')
  assert.equal(formatSubscriptionTime(''), '—')
  assert.notEqual(formatSubscriptionTime('2026-10-23T08:00:00'), '2026-10-23T08:00:00')
  assert.notEqual(formatSubscriptionTime('1792713600000'), '1792713600000')
  assert.equal(formatSubscriptionTime('1792713600', 'seconds'), formatSubscriptionTime('1792713600000'))
  assert.equal(formatSubscriptionTime('-1', 'seconds'), formatSubscriptionTime('-1000'))
  assert.equal(formatSubscriptionTime(0, 'seconds'), formatSubscriptionTime(0))
  assert.equal(formatSubscriptionTime(null, 'seconds'), '—')
  assert.equal(formatSubscriptionTime('9223372036854775807', 'seconds'), '9223372036854775807')
})
