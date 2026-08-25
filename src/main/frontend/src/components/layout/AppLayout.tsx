import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import AppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Stack from '@mui/material/Stack'
import { useTranslation } from 'react-i18next'
import Footer from './Footer'

const navLinkStyle = ({ isActive }: { isActive: boolean }) => ({
  color: '#fff',
  textDecoration: 'none',
  fontWeight: isActive ? 700 : 400,
  opacity: isActive ? 1 : 0.85,
})

export default function AppLayout({ children }: { children: ReactNode }) {
  const { t } = useTranslation()

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static" color="primary" enableColorOnDark>
        <Toolbar>
          <Box
            component="img"
            src="/favicon.png"
            alt=""
            sx={{ width: 24, height: 24, mr: 1 }}
          />
          <Box sx={{ fontWeight: 700, mr: 4 }}>PetClinic</Box>
          <Stack direction="row" spacing={3}>
            <NavLink to="/" end style={navLinkStyle}>
              {t('nav.home')}
            </NavLink>
            <NavLink to="/owners/find" style={navLinkStyle}>
              {t('nav.findOwners')}
            </NavLink>
            <NavLink to="/vets" style={navLinkStyle}>
              {t('nav.vets')}
            </NavLink>
          </Stack>
        </Toolbar>
      </AppBar>
      <Container component="main" sx={{ flex: 1, py: 4 }}>
        {children}
      </Container>
      <Footer />
    </Box>
  )
}
