import axios from 'axios'

const STORAGE_API = '/api/v1/storage/files'

export const uploadStoredFile = async ({
  file,
  moduleCode = 'EMPLOYEE',
  businessCategory = 'EMPLOYEE_CV',
  visibilityScope = 'INTERNAL',
  note,
} = {}) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('moduleCode', moduleCode)
  formData.append('businessCategory', businessCategory)
  if (visibilityScope) formData.append('visibilityScope', visibilityScope)
  if (note) formData.append('note', note)

  const response = await axios.post(STORAGE_API, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })

  return response.data
}
