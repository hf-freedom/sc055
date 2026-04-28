import React, { useState } from 'react'
import { Routes, Route, Link, useLocation } from 'react-router-dom'
import { Layout, Menu } from 'antd'
import {
  HomeOutlined,
  TeamOutlined,
  ShopOutlined,
  ShoppingOutlined,
  MoneyCollectOutlined,
  TransactionOutlined,
  ReconciliationOutlined,
} from '@ant-design/icons'
import Home from './pages/Home'
import Users from './pages/Users'
import Products from './pages/Products'
import Orders from './pages/Orders'
import Commissions from './pages/Commissions'
import Withdraws from './pages/Withdraws'
import Refunds from './pages/Refunds'

const { Header, Sider, Content } = Layout

function App() {
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: <Link to="/">首页</Link>,
    },
    {
      key: '/users',
      icon: <TeamOutlined />,
      label: <Link to="/users">用户管理</Link>,
    },
    {
      key: '/products',
      icon: <ShopOutlined />,
      label: <Link to="/products">商品管理</Link>,
    },
    {
      key: '/orders',
      icon: <ShoppingOutlined />,
      label: <Link to="/orders">订单管理</Link>,
    },
    {
      key: '/commissions',
      icon: <MoneyCollectOutlined />,
      label: <Link to="/commissions">佣金管理</Link>,
    },
    {
      key: '/withdraws',
      icon: <TransactionOutlined />,
      label: <Link to="/withdraws">提现管理</Link>,
    },
    {
      key: '/refunds',
      icon: <ReconciliationOutlined />,
      label: <Link to="/refunds">退款管理</Link>,
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{ height: 64, margin: 16, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: collapsed ? 12 : 18, fontWeight: 'bold' }}>
          {collapsed ? '分销' : '二级分销系统'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: '#fff', display: 'flex', alignItems: 'center', paddingLeft: 16 }}>
          <h2 style={{ margin: 0 }}>二级分销系统</h2>
        </Header>
        <Content style={{ margin: '16px', padding: 24, background: '#fff' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/users" element={<Users />} />
            <Route path="/products" element={<Products />} />
            <Route path="/orders" element={<Orders />} />
            <Route path="/commissions" element={<Commissions />} />
            <Route path="/withdraws" element={<Withdraws />} />
            <Route path="/refunds" element={<Refunds />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  )
}

export default App
