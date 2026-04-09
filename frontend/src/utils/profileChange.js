const FIELD_LABELS = {
  personalEmail: 'Email cá nhân',
  mobilePhone: 'Số điện thoại',
  avatarUrl: 'Ảnh đại diện',
  taxCode: 'Mã số thuế',
  personalTaxId: 'Mã số thuế',
  nationality: 'Quốc tịch',
  placeOfBirth: 'Nơi sinh',
  ethnicGroup: 'Dân tộc',
  religion: 'Tôn giáo',
  maritalStatus: 'Tình trạng hôn nhân',
  educationLevel: 'Học vấn',
  major: 'Chuyên ngành',
  emergencyNote: 'Ghi chú khẩn cấp',
  primaryAddress: 'Địa chỉ chi tiết',
  country: 'Quốc gia',
  stateProvince: 'Tỉnh / Thành phố',
  city: 'Quận / Huyện',
  postCode: 'Mã bưu chính',
  healthInsurance: 'Bảo hiểm y tế',
  socialInsurance: 'Bảo hiểm xã hội',
}

const LEGACY_FIELD_ALIASES = {
  'email ca nhan': 'personalEmail',
  'personal email': 'personalEmail',
  email: 'personalEmail',
  'so dien thoai': 'mobilePhone',
  'dien thoai': 'mobilePhone',
  'mobile phone': 'mobilePhone',
  'anh dai dien': 'avatarUrl',
  avatar: 'avatarUrl',
  'ma so thue': 'taxCode',
  mst: 'taxCode',
  'ma so thue tncn': 'personalTaxId',
  'quoc tich': 'nationality',
  'noi sinh': 'placeOfBirth',
  'dan toc': 'ethnicGroup',
  'ton giao': 'religion',
  'tinh trang hon nhan': 'maritalStatus',
  'hon nhan': 'maritalStatus',
  'hoc van': 'educationLevel',
  'trinh do hoc van': 'educationLevel',
  'chuyen nganh': 'major',
  'ghi chu khan cap': 'emergencyNote',
  'ghi chu lien he khan cap': 'emergencyNote',
  'dia chi chi tiet': 'primaryAddress',
  'quoc gia': 'country',
  'tinh thanh pho': 'stateProvince',
  'quan huyen': 'city',
  'ma buu chinh': 'postCode',
  'bao hiem y te': 'healthInsurance',
  'bao hiem xa hoi': 'socialInsurance',
}

function normalizeAlias(value) {
  return value
    ? value
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-zA-Z0-9]+/g, ' ')
        .trim()
        .toLowerCase()
    : ''
}

function toLegacyPayload(payload) {
  if (!payload?.fieldName) return {}

  const normalizedField =
    LEGACY_FIELD_ALIASES[normalizeAlias(payload.fieldName)] ||
    payload.fieldName

  if (!FIELD_LABELS[normalizedField]) return {}

  return {
    [normalizedField]: payload.newValue ?? '',
  }
}

export function parseProfileChangePayload(payloadJson) {
  if (!payloadJson) return {}

  if (typeof payloadJson === 'object') {
    return payloadJson.fieldName ? toLegacyPayload(payloadJson) : payloadJson
  }

  try {
    const parsed = JSON.parse(payloadJson)
    return parsed?.fieldName ? toLegacyPayload(parsed) : parsed
  } catch (error) {
    console.error('Failed to parse profile change payload:', error)
    return {}
  }
}

export function profileChangeEntries(payloadJson) {
  const payload = parseProfileChangePayload(payloadJson)

  return Object.entries(payload)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([field, value]) => ({
      field,
      label: FIELD_LABELS[field] || field,
      value,
    }))
}

export function summarizeProfileChange(payloadJson) {
  const entries = profileChangeEntries(payloadJson)
  if (!entries.length) return 'Không có trường thay đổi được gửi kèm.'
  if (entries.length === 1) return `Cập nhật ${entries[0].label.toLowerCase()}.`
  return `Cập nhật ${entries.length} trường hồ sơ.`
}
