import { useEffect, useState } from 'react'
import Typography from '@mui/material/Typography'
import Table from '@mui/material/Table'
import TableHead from '@mui/material/TableHead'
import TableBody from '@mui/material/TableBody'
import TableRow from '@mui/material/TableRow'
import TableCell from '@mui/material/TableCell'
import { useTranslation } from 'react-i18next'
import { vetsApi } from '../../api/vets'
import type { VetResponse } from '../../api/types'
import Pagination from '../../components/common/Pagination'

export default function VetsListPage() {
  const { t } = useTranslation()
  const [page, setPage] = useState(0)
  const [vets, setVets] = useState<VetResponse[]>([])
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    vetsApi.list(page).then((result) => {
      setVets(result.content)
      setTotalPages(result.page.totalPages)
    })
  }, [page])

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('nav.vets')}
      </Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>{t('common.name')}</TableCell>
            <TableCell>{t('common.specialties')}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {vets.map((vet) => (
            <TableRow key={vet.id}>
              <TableCell>
                {vet.firstName} {vet.lastName}
              </TableCell>
              <TableCell>
                {vet.specialties.length > 0
                  ? vet.specialties.map((s) => s.name).join(' ')
                  : t('common.none')}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  )
}
