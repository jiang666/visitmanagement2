import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  const payload = { ...data }
  if (!payload.password) {
    delete payload.password
  }
  return request({
    url: `/users/${id}`,
    method: 'put',
    data: payload
  })
}

export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

export function batchDeleteUsers(ids) {
  return request({
    url: '/users/batch-delete',
    method: 'post',
    data: ids
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'patch',
    data: { status }
  })
}

export function resetUserPassword(id, password) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'patch',
    data: { password }
  })
}

export function getUsersByRole(role) {
  return request({
    url: '/users',
    method: 'get',
    params: {
      role,
      page: 0,
      size: 1000
    }
  })
}

