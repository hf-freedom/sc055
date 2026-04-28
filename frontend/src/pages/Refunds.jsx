import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Select, message, Space, Tag, Card, Descriptions, Popconfirm } from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { refundApi, orderApi, userApi, commissionApi } from '../services/api'
import dayjs from 'dayjs'

function Refunds() {
  const [refunds, setRefunds] = useState([])
  const [orders, setOrders] = useState([])
  const [users, setUsers] = useState([])
  const [commissions, setCommissions] = useState([])
  const [loading, setLoading] = useState(false)
  const [applyModalVisible, setApplyModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedRefund, setSelectedRefund] = useState(null)
  const [applyForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [refundsRes, ordersRes, usersRes, commissionsRes] = await Promise.all([
        refundApi.getAll(),
        orderApi.getAll(),
        userApi.getAll(),
        commissionApi.getAll(),
      ])
      setRefunds(refundsRes.data || [])
      setOrders(ordersRes.data || [])
      setUsers(usersRes.data || [])
      setCommissions(commissionsRes.data || [])
    } catch (error) {
      message.error('加载数据失败')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleApply = (values) => {
    refundApi.apply(values)
      .then(() => {
        message.success('申请退款成功')
        setApplyModalVisible(false)
        applyForm.resetFields()
        loadData()
      })
      .catch(() => {
        message.error('申请退款失败')
      })
  }

  const handleApprove = (record) => {
    refundApi.approve(record.id)
      .then(() => {
        message.success('审核通过')
        loadData()
      })
      .catch(() => {
        message.error('审核失败')
      })
  }

  const handleReject = (record) => {
    refundApi.reject(record.id, '审核拒绝')
      .then(() => {
        message.success('已拒绝')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const handleComplete = (record) => {
    refundApi.complete(record.id)
      .then(() => {
        message.success('退款完成，佣金已冲回')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const showDetail = (record) => {
    setSelectedRefund(record)
    setDetailModalVisible(true)
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '待审核' },
      APPROVED: { color: 'blue', text: '已通过' },
      REJECTED: { color: 'red', text: '已拒绝' },
      COMPLETED: { color: 'green', text: '已完成' },
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
      title: '退款单号',
      dataIndex: 'refundNo',
      key: 'refundNo',
      width: 200,
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
      title: '退款金额',
      dataIndex: 'refundAmount',
      key: 'refundAmount',
      render: (text) => <Tag color="red">-¥{text}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (text) => getStatusTag(text),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
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
              <Popconfirm title="确定审核通过？" onConfirm={() => handleApprove(record)}>
                <Button type="link" size="small">
                  通过
                </Button>
              </Popconfirm>
              <Popconfirm title="确定拒绝？" onConfirm={() => handleReject(record)}>
                <Button type="link" size="small" danger>
                  拒绝
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'APPROVED' && (
            <Popconfirm title="确定完成退款？这将冲回已生成的佣金。" onConfirm={() => handleComplete(record)}>
              <Button type="link" size="small">
                完成退款
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>退款管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setApplyModalVisible(true)}>
            申请退款
          </Button>
        </Space>
      </div>

      <Card style={{ marginBottom: 16 }} size="small">
        <p style={{ margin: 0, color: '#666' }}>
          <strong>说明：</strong>
          退款流程：申请退款 → 审核通过 → 完成退款。
          完成退款时，系统会自动冲回佣金：
          <br />• 待结算/可提现佣金：直接冲回扣减
          <br />• 已提现佣金：生成负佣金账单
        </p>
      </Card>

      <Table
        columns={columns}
        dataSource={refunds}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="申请退款"
        open={applyModalVisible}
        onOk={() => applyForm.submit()}
        onCancel={() => {
          setApplyModalVisible(false)
          applyForm.resetFields()
        }}
        width={500}
      >
        <Form form={applyForm} layout="vertical" onFinish={handleApply}>
          <Form.Item
            name="orderId"
            label="选择订单"
            rules={[{ required: true, message: '请选择订单' }]}
          >
            <Select
              placeholder="请选择订单（仅已支付/已发货/已完成订单）"
              showSearch
              optionFilterProp="children"
            >
              {orders
                .filter(o => ['PAID', 'SHIPPED', 'COMPLETED'].includes(o.status))
                .map(o => (
                  <Select.Option key={o.id} value={o.id}>
                    {o.orderNo} - {o.productName} - ¥{o.totalAmount}
                  </Select.Option>
                ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="refundAmount"
            label="退款金额"
            rules={[{ required: true, message: '请输入退款金额' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入退款金额"
              min={0}
              precision={2}
              prefix="¥"
            />
          </Form.Item>
          <Form.Item
            name="remark"
            label="退款原因"
          >
            <Input.TextArea placeholder="请输入退款原因" rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="退款详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedRefund && (
          <div>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="ID">{selectedRefund.id}</Descriptions.Item>
              <Descriptions.Item label="退款单号">{selectedRefund.refundNo}</Descriptions.Item>
              <Descriptions.Item label="订单ID">{selectedRefund.orderId}</Descriptions.Item>
              <Descriptions.Item label="订单号">{selectedRefund.orderNo}</Descriptions.Item>
              <Descriptions.Item label="用户ID">{selectedRefund.userId}</Descriptions.Item>
              <Descriptions.Item label="退款金额">
                <Tag color="red">-¥{selectedRefund.refundAmount}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="状态">{getStatusTag(selectedRefund.status)}</Descriptions.Item>
              <Descriptions.Item label="备注">{selectedRefund.remark || '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间" span={2}>
                {selectedRefund.createdAt ? dayjs(selectedRefund.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="审核时间" span={2}>
                {selectedRefund.approvedAt ? dayjs(selectedRefund.approvedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="完成时间" span={2}>
                {selectedRefund.completedAt ? dayjs(selectedRefund.completedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
            </Descriptions>

            {selectedRefund.orderId && (
              <Card title="相关订单信息" style={{ marginTop: 16 }} size="small">
                {(() => {
                  const order = orders.find(o => o.id === selectedRefund.orderId)
                  if (!order) return <p>无订单信息</p>
                  return (
                    <Descriptions column={2} size="small">
                      <Descriptions.Item label="商品">{order.productName}</Descriptions.Item>
                      <Descriptions.Item label="订单金额">¥{order.totalAmount}</Descriptions.Item>
                      <Descriptions.Item label="一级佣金">¥{order.firstLevelCommissionAmount || 0}</Descriptions.Item>
                      <Descriptions.Item label="二级佣金">¥{order.secondLevelCommissionAmount || 0}</Descriptions.Item>
                    </Descriptions>
                  )
                })()}
              </Card>
            )}

            {selectedRefund.orderId && (
              <Card title="相关佣金记录（将被冲回）" style={{ marginTop: 16 }} size="small">
                {(() => {
                  const relatedCommissions = commissions.filter(c => c.orderId === selectedRefund.orderId)
                  if (relatedCommissions.length === 0) return <p>无佣金记录</p>
                  return (
                    <Table
                      dataSource={relatedCommissions}
                      columns={[
                        { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
                        {
                          title: '用户',
                          dataIndex: 'userId',
                          key: 'userId',
                          render: (text) => {
                            const u = users.find(x => x.id === text)
                            return u ? u.username : text
                          },
                        },
                        {
                          title: '层级',
                          dataIndex: 'level',
                          key: 'level',
                          render: (text) => (text === 1 ? '一级' : '二级'),
                        },
                        {
                          title: '金额',
                          dataIndex: 'amount',
                          key: 'amount',
                          render: (text) => `¥${text}`,
                        },
                        {
                          title: '状态',
                          dataIndex: 'status',
                          key: 'status',
                          render: (text) => {
                            const map = {
                              PENDING: '待结算',
                              AVAILABLE: '可提现',
                              WITHDRAWING: '提现中',
                              WITHDRAWN: '已提现',
                              REVERTED: '已冲回',
                            }
                            return map[text] || text
                          },
                        },
                      ]}
                      rowKey="id"
                      pagination={false}
                      size="small"
                    />
                  )
                })()}
              </Card>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Refunds
