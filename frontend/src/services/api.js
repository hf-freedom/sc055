import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const userApi = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  bindParent: (userId, parentId) => api.post(`/users/${userId}/bind-parent`, { parentId }),
  getRelation: (userId) => api.get(`/users/${userId}/relation`),
  getChildren: (userId) => api.get(`/users/${userId}/children`),
  getGrandchildren: (userId) => api.get(`/users/${userId}/grandchildren`),
}

export const productApi = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  decreaseStock: (id, quantity) => api.post(`/products/${id}/decrease-stock`, { quantity }),
  increaseStock: (id, quantity) => api.post(`/products/${id}/increase-stock`, { quantity }),
}

export const orderApi = {
  getAll: () => api.get('/orders'),
  getById: (id) => api.get(`/orders/${id}`),
  getByOrderNo: (orderNo) => api.get(`/orders/no/${orderNo}`),
  getByUserId: (userId) => api.get(`/orders/user/${userId}`),
  create: (data) => api.post('/orders', data),
  pay: (id) => api.post(`/orders/${id}/pay`),
  ship: (id) => api.post(`/orders/${id}/ship`),
  complete: (id) => api.post(`/orders/${id}/complete`),
  cancel: (id) => api.post(`/orders/${id}/cancel`),
}

export const commissionApi = {
  getAll: () => api.get('/commissions'),
  getById: (id) => api.get(`/commissions/${id}`),
  getByUserId: (userId) => api.get(`/commissions/user/${userId}`),
  getByOrderId: (orderId) => api.get(`/commissions/order/${orderId}`),
  getPending: () => api.get('/commissions/status/pending'),
  getAvailable: () => api.get('/commissions/status/available'),
  settleAll: () => api.post('/commissions/settle-all'),
}

export const withdrawApi = {
  getAll: () => api.get('/withdraws'),
  getById: (id) => api.get(`/withdraws/${id}`),
  getByWithdrawNo: (withdrawNo) => api.get(`/withdraws/no/${withdrawNo}`),
  getByUserId: (userId) => api.get(`/withdraws/user/${userId}`),
  getPending: () => api.get('/withdraws/status/pending'),
  apply: (data) => api.post('/withdraws/apply', data),
  approve: (id) => api.post(`/withdraws/${id}/approve`),
  reject: (id, reason) => api.post(`/withdraws/${id}/reject`, { reason }),
  pay: (id) => api.post(`/withdraws/${id}/pay`),
  fail: (id, reason) => api.post(`/withdraws/${id}/fail`, { reason }),
  compensate: (id) => api.post(`/withdraws/${id}/compensate`),
  compensateAll: () => api.post('/withdraws/compensate-all'),
}

export const refundApi = {
  getAll: () => api.get('/refunds'),
  getById: (id) => api.get(`/refunds/${id}`),
  getByRefundNo: (refundNo) => api.get(`/refunds/no/${refundNo}`),
  getByOrderId: (orderId) => api.get(`/refunds/order/${orderId}`),
  getPending: () => api.get('/refunds/status/pending'),
  apply: (data) => api.post('/refunds/apply', data),
  approve: (id) => api.post(`/refunds/${id}/approve`),
  reject: (id, reason) => api.post(`/refunds/${id}/reject`, { reason }),
  complete: (id) => api.post(`/refunds/${id}/complete`),
}

export default api
