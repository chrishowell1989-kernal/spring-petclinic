export interface PetTypeResponse {
  id: number
  name: string
}

export interface VisitResponse {
  id: number
  date: string
  description: string
}

export interface VisitRequest {
  date: string
  description: string
}

export interface PetResponse {
  id: number
  name: string
  birthDate: string
  type: PetTypeResponse
  visits: VisitResponse[]
}

export interface PetRequest {
  name: string
  birthDate: string
  typeId: number
}

export interface OwnerSummary {
  id: number
  firstName: string
  lastName: string
  address: string
  city: string
  telephone: string
  pets: PetResponse[]
}

export type OwnerResponse = OwnerSummary

export interface OwnerRequest {
  firstName: string
  lastName: string
  address: string
  city: string
  telephone: string
}

export interface SpecialtyResponse {
  id: number
  name: string
}

export interface VetResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  telephone: string
  specialties: SpecialtyResponse[]
}

export interface PagedResponse<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}
