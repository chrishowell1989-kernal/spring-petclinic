export interface FieldError {
  field: string
  code: string
  message: string
}

export class ApiError extends Error {
  status: number
  errors: FieldError[]

  constructor(status: number, errors: FieldError[]) {
    super(errors.map((e) => e.message).join(', ') || `Request failed with status ${status}`)
    this.status = status
    this.errors = errors
  }

  fieldError(field: string): string | undefined {
    return this.errors.find((e) => e.field === field)?.message
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
  })

  if (!response.ok) {
    let errors: FieldError[] = []
    try {
      const body = (await response.json()) as { errors?: FieldError[] }
      errors = body.errors ?? []
    } catch {
      // response had no JSON body — fall through with an empty error list
    }
    throw new ApiError(response.status, errors)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
}
