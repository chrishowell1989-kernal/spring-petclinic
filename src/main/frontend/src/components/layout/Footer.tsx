import Box from '@mui/material/Box'
import Container from '@mui/material/Container'

export default function Footer() {
  return (
    <Box component="footer" sx={{ bgcolor: '#f5f5f5', py: 2, mt: 'auto' }}>
      <Container sx={{ display: 'flex', justifyContent: 'center' }}>
        <Box
          component="img"
          src="/images/spring-logo.svg"
          alt="Spring"
          sx={{ height: 24, opacity: 0.7 }}
        />
      </Container>
    </Box>
  )
}
