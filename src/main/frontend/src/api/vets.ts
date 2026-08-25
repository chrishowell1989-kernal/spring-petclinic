import { apiClient } from './client'
import type { PagedResponse, VetResponse } from './types'

export const vetsApi = {
  list: (page: number, size = 5) =>
    apiClient.get<PagedResponse<VetResponse>>(`/vets?page=${page}&size=${size}`),
}
