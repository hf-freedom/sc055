import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, message, Descriptions, Tag, Card, Space, Popconfirm } from 'antd'
import { PlusOutlined, UserAddOutlined, ReloadOutlined } from '@ant-design/icons'
import { userApi } from '../services/api'
import dayjs from 'dayjs'

function Users() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [addModalVisible, setAddModalVisible] = useState(false)
  const [bindModalVisible, setBindModalVisible] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [bindForm] = Form.useForm()

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    setLoading(true)
    try {
      const response = await userApi.getAll()
      setUsers(response.data || [])
    } catch (error) {
      message.error('加载用户列表失败')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = (values) => {
    userApi.create(values)
      .then(() => {
        message.success('创建用户成功')
        setAddModalVisible(false)
        form.resetFields()
        loadUsers()
      })
      .catch(() => {
        message.error('创建用户失败')
      })
  }

  const handleBindParent = (values) => {
    if (!selectedUser) return

    userApi.bindParent(selectedUser.id, values.parentId)
      .then((response) => {
        if (response.data) {
          message.success('绑定上级成功')
          setBindModalVisible(false)
          bindForm.resetFields()
          loadUsers()
        } else {
          message.error('绑定上级失败')
        }
      })
      .catch(() => {
        message.error('绑定上级失败')
      })
  }

  const showDetail = (record) => {
    setSelectedUser(record)
    setDetailModalVisible(true)
  }

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '姓名',
      dataIndex: 'realName',
      key: 'realName',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: '上级ID',
      dataIndex: 'parentId',
      key: 'parentId',
      render: (text) => text || '-',
    },
    {
      title: '总佣金',
      dataIndex: 'totalCommission',
      key: 'totalCommission',
      render: (text) => <Tag color="blue">¥{text}</Tag>,
    },
    {
      title: '可提现',
      dataIndex: 'availableCommission',
      key: 'availableCommission',
      render: (text) => <Tag color="green">¥{text}</Tag>,
    },
    {
      title: '待结算',
      dataIndex: 'pendingCommission',
      key: 'pendingCommission',
      render: (text) => <Tag color="orange">¥{text}</Tag>,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => showDetail(record)}>
            详情
          </Button>
          {!record.parentId && (
            <Button
              type="link"
              size="small"
              icon={<UserAddOutlined />}
              onClick={() => {
                setSelectedUser(record)
                setBindModalVisible(true)
              }}
            >
              绑定上级
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>用户管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadUsers}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddModalVisible(true)}>
            新增用户
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="新增用户"
        open={addModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setAddModalVisible(false)
          form.resetFields()
        }}
      >
        <Form form={form} layout="vertical" onFinish={handleAdd}>
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item
            name="realName"
            label="姓名"
          >
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="手机号"
          >
            <Input placeholder="请输入手机号" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="绑定上级推广人"
        open={bindModalVisible}
        onOk={() => bindForm.submit()}
        onCancel={() => {
          setBindModalVisible(false)
          bindForm.resetFields()
        }}
      >
        <p>当前用户：{selectedUser?.username}</p>
        <Form form={bindForm} layout="vertical" onFinish={handleBindParent}>
          <Form.Item
            name="parentId"
            label="选择上级推广人"
            rules={[{ required: true, message: '请选择上级推广人' }]}
          >
            <Select placeholder="请选择上级推广人">
              {users
                .filter(u => u.id !== selectedUser?.id)
                .map(u => (
                  <Select.Option key={u.id} value={u.id}>
                    {u.username} - {u.realName || u.username}
                  </Select.Option>
                ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="用户详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedUser && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="ID">{selectedUser.id}</Descriptions.Item>
              <Descriptions.Item label="用户名">{selectedUser.username}</Descriptions.Item>
              <Descriptions.Item label="姓名">{selectedUser.realName || '-'}</Descriptions.Item>
              <Descriptions.Item label="手机号">{selectedUser.phone || '-'}</Descriptions.Item>
              <Descriptions.Item label="上级ID">{selectedUser.parentId || '-'}</Descriptions.Item>
              <Descriptions.Item label="总佣金">¥{selectedUser.totalCommission}</Descriptions.Item>
              <Descriptions.Item label="可提现佣金">¥{selectedUser.availableCommission}</Descriptions.Item>
              <Descriptions.Item label="待结算佣金">¥{selectedUser.pendingCommission}</Descriptions.Item>
              <Descriptions.Item label="创建时间" span={2}>
                {selectedUser.createdAt ? dayjs(selectedUser.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
            </Descriptions>

            {selectedUser.parentId && (
              <Card title="上级信息" style={{ marginTop: 16 }} size="small">
                <p>上级ID：{selectedUser.parentId}</p>
                {users.find(u => u.id === selectedUser.parentId) && (
                  <p>上级用户名：{users.find(u => u.id === selectedUser.parentId).username}</p>
                )}
              </Card>
            )}

            <Card title="下级用户" style={{ marginTop: 16 }} size="small">
              {users.filter(u => u.parentId === selectedUser.id).length > 0 ? (
                <Table
                  dataSource={users.filter(u => u.parentId === selectedUser.id)}
                  columns={[
                    { title: 'ID', dataIndex: 'id', key: 'id' },
                    { title: '用户名', dataIndex: 'username', key: 'username' },
                    { title: '姓名', dataIndex: 'realName', key: 'realName' },
                  ]}
                  rowKey="id"
                  pagination={false}
                  size="small"
                />
              ) : (
                <p>暂无下级用户</p>
              )}
            </Card>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Users
