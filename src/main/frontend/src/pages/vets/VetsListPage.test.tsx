import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { I18nextProvider } from 'react-i18next'
import i18n from '../../i18n'
import VetsListPage from './VetsListPage'
import { vetsApi } from '../../api/vets'
import type { PagedResponse, VetResponse } from '../../api/types'

vi.mock('../../api/vets', () => ({
  vetsApi: {
    list: vi.fn(),
  },
}))

const mockedList = vi.mocked(vetsApi.list)

function pageOf(vets: VetResponse[]): PagedResponse<VetResponse> {
  return {
    content: vets,
    page: { size: 5, number: 0, totalElements: vets.length, totalPages: 1 },
  }
}

describe('VetsListPage', () => {
  beforeEach(() => {
    mockedList.mockReset()
  })

  it('renders Email Address and Contact Number column headers', async () => {
    mockedList.mockResolvedValue(pageOf([]))

    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>
          <VetsListPage />
        </MemoryRouter>
      </I18nextProvider>,
    )

    expect(await screen.findByRole('columnheader', { name: 'Email Address' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Contact Number' })).toBeInTheDocument()
  })

  it('displays the vet email and contact number in UK mobile format', async () => {
    mockedList.mockResolvedValue(
      pageOf([
        {
          id: 1,
          firstName: 'James',
          lastName: 'Carter',
          email: 'james.carter@petclinic.com',
          telephone: '07700900123',
          specialties: [],
        },
      ]),
    )

    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>
          <VetsListPage />
        </MemoryRouter>
      </I18nextProvider>,
    )

    expect(await screen.findByText('james.carter@petclinic.com')).toBeInTheDocument()
    expect(screen.getByText('07700 900123')).toBeInTheDocument()
  })
})
