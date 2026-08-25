import { apiClient } from './client'
import type { OwnerRequest, OwnerResponse, PagedResponse } from './types'

export const ownersApi = {
  search: (lastName: string, page: number, size = 5) =>
    apiClient.get<PagedResponse<OwnerResponse>>(
      `/owners?lastName=${encodeURIComponent(lastName)}&page=${page}&size=${size}`,
    ),
  get: (ownerId: number) => apiClient.get<OwnerResponse>(`/owners/${ownerId}`),
  create: (request: OwnerRequest) => apiClient.post<OwnerResponse>('/owners', request),
  update: (ownerId: number, request: OwnerRequest) =>
    apiClient.put<OwnerResponse>(`/owners/${ownerId}`, request),
}
