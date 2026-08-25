import { apiClient } from './client'
import type { VisitRequest, VisitResponse } from './types'

export const visitsApi = {
  create: (ownerId: number, petId: number, request: VisitRequest) =>
    apiClient.post<VisitResponse>(`/owners/${ownerId}/pets/${petId}/visits`, request),
}
