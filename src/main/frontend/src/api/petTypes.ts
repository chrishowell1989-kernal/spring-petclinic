import { apiClient } from './client'
import type { PetTypeResponse } from './types'

export const petTypesApi = {
  list: () => apiClient.get<PetTypeResponse[]>('/pet-types'),
}
