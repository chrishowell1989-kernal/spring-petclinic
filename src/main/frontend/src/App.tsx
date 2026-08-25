import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { ThemeProvider } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { theme } from './theme'
import AppLayout from './components/layout/AppLayout'
import ErrorBoundary from './components/common/ErrorBoundary'
import HomePage from './pages/HomePage'
import NotFoundPage from './pages/NotFoundPage'
import ErrorPage from './pages/ErrorPage'
import FindOwnersPage from './pages/owners/FindOwnersPage'
import OwnersListPage from './pages/owners/OwnersListPage'
import OwnerDetailsPage from './pages/owners/OwnerDetailsPage'
import OwnerFormPage from './pages/owners/OwnerFormPage'
import PetFormPage from './pages/pets/PetFormPage'
import VisitFormPage from './pages/pets/VisitFormPage'
import VetsListPage from './pages/vets/VetsListPage'
import CrashPage from './pages/CrashPage'
import './i18n'

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <ErrorBoundary>
          <BrowserRouter>
            <AppLayout>
              <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/owners/find" element={<FindOwnersPage />} />
                <Route path="/owners" element={<OwnersListPage />} />
                <Route path="/owners/new" element={<OwnerFormPage />} />
                <Route path="/owners/:ownerId" element={<OwnerDetailsPage />} />
                <Route path="/owners/:ownerId/edit" element={<OwnerFormPage />} />
                <Route path="/owners/:ownerId/pets/new" element={<PetFormPage />} />
                <Route path="/owners/:ownerId/pets/:petId/edit" element={<PetFormPage />} />
                <Route
                  path="/owners/:ownerId/pets/:petId/visits/new"
                  element={<VisitFormPage />}
                />
                <Route path="/vets" element={<VetsListPage />} />
                <Route path="/oups" element={<CrashPage />} />
                <Route path="/error" element={<ErrorPage status={500} />} />
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </AppLayout>
          </BrowserRouter>
        </ErrorBoundary>
      </LocalizationProvider>
    </ThemeProvider>
  )
}
