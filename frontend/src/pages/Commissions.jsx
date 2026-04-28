import React, { useState, useEffect } from 'react'
import { Table, Button, message, Space, Tag, Card, Descriptions, Modal } from 'antd'
import { ReloadOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { commissionApi, userApi, orderApi } from '../services/api'
import dayjs from 'dayjs'

function Commissions() {
  const [commissions, setCommissions] = useState([])
  const [users, setUsers] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedCommission, setSelectedCommission] = useState(null)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [commissionsRes, usersRes, ordersRes] = await Promise.all([
        commissionApi.getAll(),
        userApi.getAll(),
        orderApi.getAll(),
      ])
      setCommissions(commissionsRes.data || [])
      setUsers(usersRes.data || [])
      setOrders(ordersRes.data || [])
    } catch (error) {
      message.error('加载数据失败')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleSettleAll = () => {
    commissionApi.settleAll()
      .then(() => {
        message.success('已触发结算')
        loadData()
      })
      .catch(() => {
        message.error('结算失败')
      })
  }

  const showDetail = (record) => {
    setSelectedCommission(record)
    setDetailModalVisible(true)
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '待结算' },
      AVAILABLE: { color: 'green', text: '可提现' },
      WITHDRAWING: { color: 'blue', text: '提现中' },
      WITHDRAWN: { color: 'default', text: '已提现' },
      REVERTED: { color: 'red', text: '已冲回' },
      CANCELLED: { color: 'default', text: '已取消' },
    }
    const info = statusMap[status] || { color: 'default', text: status }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const getLevelTag = (level) => {
    if (!level) return '-'
    const color = level === 1 ? 'blue' : 'purple'
    return <Tag color={color}>{level === 1 ? '一级' : '二级'}</Tag>
  }

  const getAmountDisplay = (amount) => {
    if (!amount) return amount
    return '-'
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
      title: '来源用户',
      dataIndex: 'fromUserId',
      key: 'fromUserId',
      render: (text) => {
        if (!text) return '-'
        const user = users.find(u => u.id === text)
        return user ? user.username : text
      },
    },
    {
      title: '层级',
      dataIndex: 'level',
      key: 'level',
      render: (text) => getLevelTag(text),
    },
    {
      title: '金额',
      dataIndex: 'amount',
      key: 'amount',
      render: (text) => {
        const isNegative = text && text < 0
        return (
          <Tag color={isNegative ? 'red' : 'green'}>
          {text >= 0 ? '+' : ''}¥{text}
        </Tag>
        )
      },
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
      width: 100,
      render: (_, record) => (
        <Button type="link" size="small" onClick={() => showDetail(record)}>
          详情
        </Button>
      ),
    },
  ]

  const pendingCount = commissions.filter(c => c.status === 'PENDING').length
  const availableCount = commissions.filter(c => c.status === 'AVAILABLE').length
  const withdrawnCount = commissions.filter(c => c.status === 'WITHDRAWN').length
  const revertedCount = commissions.filter(c => c.status === 'REVERTED').length

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>佣金管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button
            type="primary"
            icon={<ThunderboltOutlined />}
            onClick={handleSettleAll}
          >
            立即结算
          </Button>
        </Space>
      </div>

      <Card style={{ marginBottom: 16 }} size="small">
        <Space>
          <Tag color="orange">待结算：{pendingCount} 条</Tag>
          <Tag color="green">可提现：{availableCount} 条</Tag>
          <Tag color="default">已提现：{withdrawnCount} 条</Tag>
          <Tag color="red">已冲回：{revertedCount} 条</Tag>
        </Space>
        <p style={{ margin: '8px 0 0 0', color: '#666', fontSize: 12 }}>
          <strong>说明：</strong>
          订单支付后佣金进入待结算；订单完成且过售后期（默认7天）后，系统定时任务自动结算为可提现。
          点击"立即结算"可手动触发结算（用于测试）。
        </p>
      </Card>

      <Table
        columns={columns}
        dataSource={commissions}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="佣金详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={600}
      >
        {selectedCommission && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="ID">{selectedCommission.id}</Descriptions.Item>
            <Descriptions.Item label="订单ID">{selectedCommission.orderId || '-'}</Descriptions.Item>
            <Descriptions.Item label="订单号">{selectedCommission.orderNo || '-'}</Descriptions.Item>
            <Descriptions.Item label="用户ID">{selectedCommission.userId}</Descriptions.Item>
            <Descriptions.Item label="来源用户ID">{selectedCommission.fromUserId || '-'}</Descriptions.Item>
            <Descriptions.Item label="层级">
              {selectedCommission.level ? (selectedCommission.level === 1 ? '一级' : '二级') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="金额">
              <Tag color={selectedCommission.amount >= 0 ? 'green' : 'red'}>
                {selectedCommission.amount >= 0 ? '+' : ''}¥{selectedCommission.amount}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              {getStatusTag(selectedCommission.status)}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {selectedCommission.createdAt ? dayjs(selectedCommission.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="结算时间" span={2}>
              {selectedCommission.settledAt ? dayjs(selectedCommission.settledAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="备注" span={2}>
              {selectedCommission.remark || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="关联佣金ID" span={2}>
              {selectedCommission.relatedCommissionId || '-'}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  )
}

export default Commissions
