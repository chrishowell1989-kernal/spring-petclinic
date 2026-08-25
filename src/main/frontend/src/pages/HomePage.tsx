import { Link as RouterLink } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Grid from '@mui/material/Grid'
import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import SearchIcon from '@mui/icons-material/Search'
import ListAltIcon from '@mui/icons-material/ListAlt'
import PersonAddIcon from '@mui/icons-material/PersonAdd'
import { useTranslation } from 'react-i18next'

const cards = [
  { to: '/owners/find', icon: <SearchIcon fontSize="large" color="primary" />, key: 'findOwnersCard' as const },
  { to: '/vets', icon: <ListAltIcon fontSize="large" color="primary" />, key: 'vetsCard' as const },
  { to: '/owners/new', icon: <PersonAddIcon fontSize="large" color="primary" />, key: 'addOwnerCard' as const },
]

export default function HomePage() {
  const { t } = useTranslation()

  return (
    <Stack spacing={4} sx={{ alignItems: 'center', textAlign: 'center' }}>
      <Box>
        <Typography variant="h3" component="h1" gutterBottom>
          {t('home.welcome')}
        </Typography>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {t('home.tagline')}
        </Typography>
        <Button
          component={RouterLink}
          to="/owners/find"
          variant="contained"
          size="large"
          startIcon={<SearchIcon />}
          sx={{ mt: 2 }}
        >
          {t('home.findOwnersCard')}
        </Button>
      </Box>

      <Grid container spacing={3} sx={{ width: '100%' }}>
        {cards.map((card) => (
          <Grid key={card.to} size={{ xs: 12, md: 4 }}>
            <Card>
              <CardActionArea component={RouterLink} to={card.to}>
                <CardContent sx={{ textAlign: 'center', py: 4 }}>
                  {card.icon}
                  <Typography variant="h6" sx={{ mt: 1 }}>
                    {t(`home.${card.key}`)}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Box
        component="img"
        src="/images/pets.png"
        alt=""
        sx={{ maxWidth: '100%' }}
      />
    </Stack>
  )
}
