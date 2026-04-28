import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Descriptions, Tag } from 'antd'
import {
  TeamOutlined,
  ShopOutlined,
  ShoppingOutlined,
  MoneyCollectOutlined,
  TransactionOutlined,
  ReconciliationOutlined,
} from '@ant-design/icons'
import { userApi, productApi, orderApi, commissionApi, withdrawApi, refundApi } from '../services/api'

function Home() {
  const [stats, setStats] = useState({
    users: 0,
    products: 0,
    orders: 0,
    pendingCommissions: 0,
    availableCommissions: 0,
    withdraws: 0,
    refunds: 0,
  })

  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      const [users, products, orders, pending, available, withdraws, refunds] = await Promise.all([
        userApi.getAll(),
        productApi.getAll(),
        orderApi.getAll(),
        commissionApi.getPending(),
        commissionApi.getAvailable(),
        withdrawApi.getAll(),
        refundApi.getAll(),
      ])

      setStats({
        users: users.data?.length || 0,
        products: products.data?.length || 0,
        orders: orders.data?.length || 0,
        pendingCommissions: pending.data?.length || 0,
        availableCommissions: available.data?.length || 0,
        withdraws: withdraws.data?.length || 0,
        refunds: refunds.data?.length || 0,
      })
    } catch (error) {
      console.error('Failed to load stats:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2>系统概览</h2>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="用户总数"
              value={stats.users}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="商品总数"
              value={stats.products}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="订单总数"
              value={stats.orders}
              prefix={<ShoppingOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="待结算佣金"
              value={stats.pendingCommissions}
              prefix={<MoneyCollectOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="可提现佣金"
              value={stats.availableCommissions}
              prefix={<MoneyCollectOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="提现记录"
              value={stats.withdraws}
              prefix={<TransactionOutlined />}
              valueStyle={{ color: '#13c2c2' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="退款记录"
              value={stats.refunds}
              prefix={<ReconciliationOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="系统说明">
        <Descriptions column={1}>
          <Descriptions.Item label="分销层级">最多两级分销</Descriptions.Item>
          <Descriptions.Item label="佣金结算">订单完成且过售后期后，定时任务自动转为可提现</Descriptions.Item>
          <Descriptions.Item label="退款冲回">退款订单会冲回待结算或已结算佣金</Descriptions.Item>
          <Descriptions.Item label="已提现退款">已提现佣金发生退款时，生成负佣金账单</Descriptions.Item>
          <Descriptions.Item label="提现失败">提现失败时，补偿任务自动退回佣金</Descriptions.Item>
          <Descriptions.Item label="关系变更">用户关系变更只影响新订单，历史订单按下单时关系计算</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="基础数据" style={{ marginTop: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="测试用户">
            <div>admin / 123456 (管理员)</div>
            <div>user1 / 123456 (用户一，无上级)</div>
            <div>user2 / 123456 (用户二，上级 user1)</div>
            <div>user3 / 123456 (用户三，上级 user2)</div>
          </Descriptions.Item>
          <Descriptions.Item label="测试商品">
            <div>测试商品A - 支持分销 (一级10%，二级5%)</div>
            <div>测试商品B - 不参与分销</div>
            <div>测试商品C - 高佣金 (一级15%，二级8%)</div>
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  )
}

export default Home
