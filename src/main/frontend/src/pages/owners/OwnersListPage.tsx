import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams, Link as RouterLink } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Table from '@mui/material/Table'
import TableHead from '@mui/material/TableHead'
import TableBody from '@mui/material/TableBody'
import TableRow from '@mui/material/TableRow'
import TableCell from '@mui/material/TableCell'
import Alert from '@mui/material/Alert'
import { useTranslation } from 'react-i18next'
import { ownersApi } from '../../api/owners'
import type { OwnerResponse } from '../../api/types'
import Pagination from '../../components/common/Pagination'

export default function OwnersListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const lastName = searchParams.get('lastName') ?? ''
  const page = Number(searchParams.get('page') ?? '0')

  const [owners, setOwners] = useState<OwnerResponse[] | null>(null)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    let cancelled = false
    ownersApi.search(lastName, page).then((result) => {
      if (cancelled) return
      if (result.content.length === 1) {
        navigate(`/owners/${result.content[0].id}`, { replace: true })
        return
      }
      setOwners(result.content)
      setTotalPages(result.page.totalPages)
    })
    return () => {
      cancelled = true
    }
  }, [lastName, page, navigate])

  const handlePageChange = (newPage: number) => {
    setSearchParams({ lastName, page: String(newPage) })
  }

  if (owners === null) {
    return <Typography>{t('common.loading')}</Typography>
  }

  if (owners.length === 0) {
    return <Alert severity="warning">{`"${lastName}" ${t('owners.notFound')}`}</Alert>
  }

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('owners.listTitle')}
      </Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>{t('common.name')}</TableCell>
            <TableCell>{t('owners.tableAddress')}</TableCell>
            <TableCell>{t('common.city')}</TableCell>
            <TableCell>{t('common.telephone')}</TableCell>
            <TableCell>{t('owners.tablePets')}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {owners.map((owner) => (
            <TableRow key={owner.id}>
              <TableCell>
                <RouterLink to={`/owners/${owner.id}`}>
                  {owner.firstName} {owner.lastName}
                </RouterLink>
              </TableCell>
              <TableCell>{owner.address}</TableCell>
              <TableCell>{owner.city}</TableCell>
              <TableCell>{owner.telephone}</TableCell>
              <TableCell>{owner.pets.map((pet) => pet.name).join(', ')}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Pagination page={page} totalPages={totalPages} onPageChange={handlePageChange} />
    </div>
  )
}
