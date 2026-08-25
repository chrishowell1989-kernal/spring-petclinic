import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { I18nextProvider } from 'react-i18next'
import i18n from '../i18n'
import HomePage from './HomePage'

describe('HomePage', () => {
  it('renders a Welcome heading', () => {
    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>
          <HomePage />
        </MemoryRouter>
      </I18nextProvider>,
    )
    expect(screen.getByRole('heading', { level: 1, name: 'Welcome' })).toBeInTheDocument()
  })
})
