import { apiClient } from './client'
import type { PetRequest, PetResponse } from './types'

export const petsApi = {
  create: (ownerId: number, request: PetRequest) =>
    apiClient.post<PetResponse>(`/owners/${ownerId}/pets`, request),
  update: (ownerId: number, petId: number, request: PetRequest) =>
    apiClient.put<PetResponse>(`/owners/${ownerId}/pets/${petId}`, request),
}
