import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Select, message, Space, Tag, Card, Descriptions, Popconfirm } from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { withdrawApi, userApi } from '../services/api'
import dayjs from 'dayjs'

function Withdraws() {
  const [withdraws, setWithdraws] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [applyModalVisible, setApplyModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedWithdraw, setSelectedWithdraw] = useState(null)
  const [applyForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [withdrawsRes, usersRes] = await Promise.all([
        withdrawApi.getAll(),
        userApi.getAll(),
      ])
      setWithdraws(withdrawsRes.data || [])
      setUsers(usersRes.data || [])
    } catch (error) {
      message.error('加载数据失败')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleApply = (values) => {
    withdrawApi.apply(values)
      .then(() => {
        message.success('申请提现成功')
        setApplyModalVisible(false)
        applyForm.resetFields()
        loadData()
      })
      .catch(() => {
        message.error('申请提现失败')
      })
  }

  const handleApprove = (record) => {
    withdrawApi.approve(record.id)
      .then(() => {
        message.success('审核通过')
        loadData()
      })
      .catch(() => {
        message.error('审核失败')
      })
  }

  const handleReject = (record) => {
    withdrawApi.reject(record.id, '审核拒绝')
      .then(() => {
        message.success('已拒绝')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const handlePay = (record) => {
    withdrawApi.pay(record.id)
      .then(() => {
        message.success('已打款')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const handleMarkFailed = (record) => {
    withdrawApi.fail(record.id, '提现失败测试')
      .then(() => {
        message.success('已标记为失败')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const handleCompensate = (record) => {
    withdrawApi.compensate(record.id)
      .then(() => {
        message.success('已补偿退回佣金')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const handleCompensateAll = () => {
    withdrawApi.compensateAll()
      .then(() => {
        message.success('已补偿所有失败提现')
        loadData()
      })
      .catch(() => {
        message.error('操作失败')
      })
  }

  const showDetail = (record) => {
    setSelectedWithdraw(record)
    setDetailModalVisible(true)
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '待审核' },
      APPROVED: { color: 'blue', text: '已通过' },
      REJECTED: { color: 'red', text: '已拒绝' },
      PAID: { color: 'green', text: '已打款' },
      FAILED: { color: 'red', text: '失败' },
      COMPENSATED: { color: 'default', text: '已补偿' },
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
      title: '提现单号',
      dataIndex: 'withdrawNo',
      key: 'withdrawNo',
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
      title: '提现金额',
      dataIndex: 'amount',
      key: 'amount',
      render: (text) => <Tag color="blue">¥{text}</Tag>,
    },
    {
      title: '实际到账',
      dataIndex: 'actualAmount',
      key: 'actualAmount',
      render: (text) => `¥${text}`,
    },
    {
      title: '银行卡',
      dataIndex: 'bankAccount',
      key: 'bankAccount',
      render: (text) => text || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (text) => getStatusTag(text),
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
      width: 300,
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
            <>
              <Popconfirm title="确定打款？" onConfirm={() => handlePay(record)}>
                <Button type="link" size="small">
                  打款
                </Button>
              </Popconfirm>
              <Popconfirm title="标记为失败？" onConfirm={() => handleMarkFailed(record)}>
                <Button type="link" size="small" danger>
                  标记失败
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'FAILED' && (
            <Popconfirm title="确定退回佣金？" onConfirm={() => handleCompensate(record)}>
              <Button type="link" size="small">
                补偿退回
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
        <h2>提现管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button onClick={handleCompensateAll}>
            补偿所有失败
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setApplyModalVisible(true)}>
            申请提现
          </Button>
        </Space>
      </div>

      <Card style={{ marginBottom: 16 }} size="small">
        <p style={{ margin: 0, color: '#666' }}>
          <strong>说明：</strong>
          申请提现 → 审核通过（扣减可提现佣金）→ 打款 → 完成。
          如果打款失败，可以标记为失败状态，然后补偿退回佣金。
          最低提现金额：¥10.00
        </p>
      </Card>

      <Table
        columns={columns}
        dataSource={withdraws}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="申请提现"
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
            name="userId"
            label="选择用户"
            rules={[{ required: true, message: '请选择用户' }]}
          >
            <Select placeholder="请选择用户">
              {users.map(u => (
                <Select.Option key={u.id} value={u.id}>
                  {u.username} - 可提现: ¥{u.availableCommission}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="amount"
            label="提现金额"
            rules={[{ required: true, message: '请输入提现金额' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入提现金额，最低¥10.00"
              min={10}
              precision={2}
              prefix="¥"
            />
          </Form.Item>
          <Form.Item
            name="bankName"
            label="银行名称"
            rules={[{ required: true, message: '请输入银行名称' }]}
          >
            <Input placeholder="请输入银行名称，如：中国工商银行" />
          </Form.Item>
          <Form.Item
            name="bankAccount"
            label="银行卡号"
            rules={[{ required: true, message: '请输入银行卡号' }]}
          >
            <Input placeholder="请输入银行卡号" />
          </Form.Item>
          <Form.Item
            name="bankAccountName"
            label="开户人姓名"
            rules={[{ required: true, message: '请输入开户人姓名' }]}
          >
            <Input placeholder="请输入开户人姓名" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="提现详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={600}
      >
        {selectedWithdraw && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="ID">{selectedWithdraw.id}</Descriptions.Item>
            <Descriptions.Item label="提现单号">{selectedWithdraw.withdrawNo}</Descriptions.Item>
            <Descriptions.Item label="用户ID">{selectedWithdraw.userId}</Descriptions.Item>
            <Descriptions.Item label="提现金额">¥{selectedWithdraw.amount}</Descriptions.Item>
            <Descriptions.Item label="手续费">¥{selectedWithdraw.fee}</Descriptions.Item>
            <Descriptions.Item label="实际到账">¥{selectedWithdraw.actualAmount}</Descriptions.Item>
            <Descriptions.Item label="银行名称">{selectedWithdraw.bankName}</Descriptions.Item>
            <Descriptions.Item label="银行卡号">{selectedWithdraw.bankAccount}</Descriptions.Item>
            <Descriptions.Item label="开户人">{selectedWithdraw.bankAccountName}</Descriptions.Item>
            <Descriptions.Item label="状态">{getStatusTag(selectedWithdraw.status)}</Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {selectedWithdraw.createdAt ? dayjs(selectedWithdraw.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="审核时间" span={2}>
              {selectedWithdraw.approvedAt ? dayjs(selectedWithdraw.approvedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="打款时间" span={2}>
              {selectedWithdraw.paidAt ? dayjs(selectedWithdraw.paidAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="失败时间" span={2}>
              {selectedWithdraw.failedAt ? dayjs(selectedWithdraw.failedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="失败原因" span={2}>
              {selectedWithdraw.failReason || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="备注" span={2}>
              {selectedWithdraw.remark || '-'}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  )
}

export default Withdraws
