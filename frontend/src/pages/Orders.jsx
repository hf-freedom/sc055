import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, InputNumber, Select, message, Space, Tag, Descriptions, Card, Popconfirm } from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { orderApi, productApi, userApi } from '../services/api'
import dayjs from 'dayjs'

function Orders() {
  const [orders, setOrders] = useState([])
  const [products, setProducts] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [createForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [ordersRes, productsRes, usersRes] = await Promise.all([
        orderApi.getAll(),
        productApi.getAll(),
        userApi.getAll(),
      ])
      setOrders(ordersRes.data || [])
      setProducts(productsRes.data || [])
      setUsers(usersRes.data || [])
    } catch (error) {
      message.error('加载数据失败')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = (values) => {
    orderApi.create(values)
      .then(() => {
        message.success('创建订单成功')
        setCreateModalVisible(false)
        createForm.resetFields()
        loadData()
      })
      .catch(() => {
        message.error('创建订单失败')
      })
  }

  const handlePay = (record) => {
    orderApi.pay(record.id)
      .then(() => {
        message.success('支付成功')
        loadData()
      })
      .catch(() => {
        message.error('支付失败')
      })
  }

  const handleShip = (record) => {
    orderApi.ship(record.id)
      .then(() => {
        message.success('发货成功')
        loadData()
      })
      .catch(() => {
        message.error('发货失败')
      })
  }

  const handleComplete = (record) => {
    orderApi.complete(record.id)
      .then(() => {
        message.success('订单完成')
        loadData()
      })
      .catch(() => {
        message.error('完成订单失败')
      })
  }

  const handleCancel = (record) => {
    orderApi.cancel(record.id)
      .then(() => {
        message.success('订单已取消')
        loadData()
      })
      .catch(() => {
        message.error('取消订单失败')
      })
  }

  const showDetail = (record) => {
    setSelectedOrder(record)
    setDetailModalVisible(true)
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'default', text: '待支付' },
      PAID: { color: 'blue', text: '已支付' },
      SHIPPED: { color: 'cyan', text: '已发货' },
      COMPLETED: { color: 'green', text: '已完成' },
      CANCELLED: { color: 'red', text: '已取消' },
      REFUNDING: { color: 'orange', text: '退款中' },
      REFUNDED: { color: 'red', text: '已退款' },
    }
    const info = statusMap[status] || { color: 'default', text: status }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 200,
    },
    {
      title: '用户',
      dataIndex: 'userId',
      key: 'userId',
      render: (text) => {
        const user = users.find(u => u.id === text)
        return user ? `${user.username} (${user.realName || ''})` : text
      },
    },
    {
      title: '商品',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 80,
    },
    {
      title: '总金额',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (text) => <Tag color="blue">¥{text}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (text) => getStatusTag(text),
    },
    {
      title: '一级佣金',
      dataIndex: 'firstLevelCommissionAmount',
      key: 'firstLevelCommissionAmount',
      render: (text) => text ? `¥${text}` : '-',
    },
    {
      title: '二级佣金',
      dataIndex: 'secondLevelCommissionAmount',
      key: 'secondLevelCommissionAmount',
      render: (text) => text ? `¥${text}` : '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => text ? dayjs(text).format('MM-DD HH:mm') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => showDetail(record)}>
            详情
          </Button>
          {record.status === 'PENDING' && (
            <>
              <Button type="link" size="small" onClick={() => handlePay(record)}>
                支付
              </Button>
              <Popconfirm title="确定取消订单？" onConfirm={() => handleCancel(record)}>
                <Button type="link" size="small" danger>
                  取消
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'PAID' && (
            <Button type="link" size="small" onClick={() => handleShip(record)}>
              发货
            </Button>
          )}
          {(record.status === 'PAID' || record.status === 'SHIPPED') && (
            <Button type="link" size="small" onClick={() => handleComplete(record)}>
              完成
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>订单管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
            创建订单
          </Button>
        </Space>
      </div>

      <Card style={{ marginBottom: 16 }} size="small">
        <p style={{ margin: 0, color: '#666' }}>
          <strong>说明：</strong>
          订单支付成功后，佣金进入待结算状态；订单完成且过售后期（默认7天）后，佣金转为可提现。
          测试时可点击"完成"按钮快速完成订单，然后通过佣金管理页面的"立即结算"按钮手动触发结算。
        </p>
      </Card>

      <Table
        columns={columns}
        dataSource={orders}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1200 }}
      />

      <Modal
        title="创建订单"
        open={createModalVisible}
        onOk={() => createForm.submit()}
        onCancel={() => {
          setCreateModalVisible(false)
          createForm.resetFields()
        }}
        width={500}
      >
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="userId"
            label="选择用户"
            rules={[{ required: true, message: '请选择用户' }]}
          >
            <Select placeholder="请选择用户">
              {users.map(u => (
                <Select.Option key={u.id} value={u.id}>
                  {u.username} - {u.realName || u.username}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="productId"
            label="选择商品"
            rules={[{ required: true, message: '请选择商品' }]}
          >
            <Select placeholder="请选择商品">
              {products.map(p => (
                <Select.Option key={p.id} value={p.id}>
                  {p.name} - ¥{p.price} (库存: {p.stock})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="quantity"
            label="购买数量"
            rules={[{ required: true, message: '请输入购买数量' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入购买数量"
              min={1}
              defaultValue={1}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="订单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedOrder && (
          <div>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="订单ID">{selectedOrder.id}</Descriptions.Item>
              <Descriptions.Item label="订单号">{selectedOrder.orderNo}</Descriptions.Item>
              <Descriptions.Item label="用户ID">{selectedOrder.userId}</Descriptions.Item>
              <Descriptions.Item label="商品ID">{selectedOrder.productId}</Descriptions.Item>
              <Descriptions.Item label="商品名称">{selectedOrder.productName}</Descriptions.Item>
              <Descriptions.Item label="商品单价">¥{selectedOrder.productPrice}</Descriptions.Item>
              <Descriptions.Item label="购买数量">{selectedOrder.quantity}</Descriptions.Item>
              <Descriptions.Item label="订单金额">¥{selectedOrder.totalAmount}</Descriptions.Item>
              <Descriptions.Item label="订单状态">{getStatusTag(selectedOrder.status)}</Descriptions.Item>
              <Descriptions.Item label="上级ID">{selectedOrder.parentId || '-'}</Descriptions.Item>
              <Descriptions.Item label="上上级ID">{selectedOrder.grandparentId || '-'}</Descriptions.Item>
              <Descriptions.Item label="一级佣金比例">{(selectedOrder.firstLevelCommissionRate * 100).toFixed(2)}%</Descriptions.Item>
              <Descriptions.Item label="二级佣金比例">{(selectedOrder.secondLevelCommissionRate * 100).toFixed(2)}%</Descriptions.Item>
              <Descriptions.Item label="一级佣金金额">¥{selectedOrder.firstLevelCommissionAmount}</Descriptions.Item>
              <Descriptions.Item label="二级佣金金额">¥{selectedOrder.secondLevelCommissionAmount}</Descriptions.Item>
              <Descriptions.Item label="创建时间" span={2}>
                {selectedOrder.createdAt ? dayjs(selectedOrder.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="支付时间" span={2}>
                {selectedOrder.paidAt ? dayjs(selectedOrder.paidAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="完成时间" span={2}>
                {selectedOrder.completedAt ? dayjs(selectedOrder.completedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="售后期截止" span={2}>
                {selectedOrder.afterSalesDeadline ? dayjs(selectedOrder.afterSalesDeadline).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
            </Descriptions>

            <Card title="订单快照说明" style={{ marginTop: 16 }} size="small">
              <p style={{ margin: 0, color: '#666' }}>
                订单中的上级ID、佣金比例等信息是下单时的快照数据。即使后续用户关系变更或商品佣金比例调整，
                历史订单仍按下单时的关系计算佣金。
              </p>
            </Card>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Orders
