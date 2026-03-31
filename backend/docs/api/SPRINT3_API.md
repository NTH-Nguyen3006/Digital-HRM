# Sprint 3 API Contract - Contract Legal Core

## Contract Type
- `GET /api/v1/admin/contract-types`
- `GET /api/v1/admin/contract-types/options`
- `GET /api/v1/admin/contract-types/{contractTypeId}`
- `POST /api/v1/admin/contract-types`
- `PUT /api/v1/admin/contract-types/{contractTypeId}`
- `PATCH /api/v1/admin/contract-types/{contractTypeId}/status`

## Labor Contract
- `GET /api/v1/admin/contracts`
- `GET /api/v1/admin/contracts/expiring?days=30`
- `GET /api/v1/admin/contracts/{laborContractId}`
- `GET /api/v1/admin/contracts/{laborContractId}/history`
- `POST /api/v1/admin/contracts`
- `PUT /api/v1/admin/contracts/{laborContractId}`
- `DELETE /api/v1/admin/contracts/{laborContractId}`
- `PATCH /api/v1/admin/contracts/{laborContractId}/submit`
- `PATCH /api/v1/admin/contracts/{laborContractId}/review`
- `PATCH /api/v1/admin/contracts/{laborContractId}/activate`
- `PATCH /api/v1/admin/contracts/{laborContractId}/lifecycle-status`
- `POST /api/v1/admin/contracts/{laborContractId}/renewal-draft`

## Appendix
- `GET /api/v1/admin/contracts/{laborContractId}/appendices`
- `POST /api/v1/admin/contracts/{laborContractId}/appendices`
- `PUT /api/v1/admin/contracts/{laborContractId}/appendices/{appendixId}`
- `PATCH /api/v1/admin/contracts/{laborContractId}/appendices/{appendixId}/activate`
- `PATCH /api/v1/admin/contracts/{laborContractId}/appendices/{appendixId}/cancel`

## Attachment metadata
- `GET /api/v1/admin/contracts/{laborContractId}/attachments`
- `POST /api/v1/admin/contracts/{laborContractId}/attachments`
- `PUT /api/v1/admin/contracts/{laborContractId}/attachments/{attachmentId}`
- `PATCH /api/v1/admin/contracts/{laborContractId}/attachments/{attachmentId}/archive`
- `DELETE /api/v1/admin/contracts/{laborContractId}/attachments/{attachmentId}`
