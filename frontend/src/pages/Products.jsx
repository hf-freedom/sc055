import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Switch, message, Space, Tag, Descriptions, Card } from 'antd'
import { PlusOutlined, EditOutlined, ReloadOutlined } from '@ant-design/icons'
import { productApi } from '../services/api'
import dayjs from 'dayjs'

function Products() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(false)
  const [addModalVisible, setAddModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [addForm] = Form.useForm()
  const [editForm] = Form.useForm()

  useEffect(() => {
    loadProducts()
  }, [])

  const loadProducts = async () => {
    setLoading(true)
    try {
      const response = await productApi.getAll()
      setProducts(response.data || [])
    } catch (error) {
      message.error('加载商品列表失败')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = (values) => {
    const productData = {
      ...values,
      isDistributionEnabled: values.isDistributionEnabled || false,
      firstLevelCommissionRate: values.isDistributionEnabled ? values.firstLevelCommissionRate : 0,
      secondLevelCommissionRate: values.isDistributionEnabled ? values.secondLevelCommissionRate : 0,
    }

    productApi.create(productData)
      .then(() => {
        message.success('创建商品成功')
        setAddModalVisible(false)
        addForm.resetFields()
        loadProducts()
      })
      .catch(() => {
        message.error('创建商品失败')
      })
  }

  const handleEdit = (values) => {
    if (!selectedProduct) return

    const productData = {
      ...values,
      firstLevelCommissionRate: values.isDistributionEnabled ? values.firstLevelCommissionRate : 0,
      secondLevelCommissionRate: values.isDistributionEnabled ? values.secondLevelCommissionRate : 0,
    }

    productApi.update(selectedProduct.id, productData)
      .then(() => {
        message.success('更新商品成功')
        setEditModalVisible(false)
        loadProducts()
      })
      .catch(() => {
        message.error('更新商品失败')
      })
  }

  const openEditModal = (record) => {
    setSelectedProduct(record)
    editForm.setFieldsValue(record)
    setEditModalVisible(true)
  }

  const showDetail = (record) => {
    setSelectedProduct(record)
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
      title: '商品名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      render: (text) => `¥${text}`,
    },
    {
      title: '库存',
      dataIndex: 'stock',
      key: 'stock',
    },
    {
      title: '是否分销',
      dataIndex: 'isDistributionEnabled',
      key: 'isDistributionEnabled',
      render: (text) => (
        <Tag color={text ? 'green' : 'default'}>
          {text ? '是' : '否'}
        </Tag>
      ),
    },
    {
      title: '一级佣金比例',
      dataIndex: 'firstLevelCommissionRate',
      key: 'firstLevelCommissionRate',
      render: (text) => `${(text * 100).toFixed(2)}%`,
    },
    {
      title: '二级佣金比例',
      dataIndex: 'secondLevelCommissionRate',
      key: 'secondLevelCommissionRate',
      render: (text) => `${(text * 100).toFixed(2)}%`,
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
      width: 180,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => showDetail(record)}>
            详情
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
            编辑
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>商品管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadProducts}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddModalVisible(true)}>
            新增商品
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={products}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="新增商品"
        open={addModalVisible}
        onOk={() => addForm.submit()}
        onCancel={() => {
          setAddModalVisible(false)
          addForm.resetFields()
        }}
        width={600}
      >
        <Form form={addForm} layout="vertical" onFinish={handleAdd} initialValues={{ isDistributionEnabled: false }}>
          <Form.Item
            name="name"
            label="商品名称"
            rules={[{ required: true, message: '请输入商品名称' }]}
          >
            <Input placeholder="请输入商品名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="商品描述"
          >
            <Input.TextArea placeholder="请输入商品描述" rows={3} />
          </Form.Item>
          <Form.Item
            name="price"
            label="商品价格"
            rules={[{ required: true, message: '请输入商品价格' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入商品价格"
              min={0}
              precision={2}
              prefix="¥"
            />
          </Form.Item>
          <Form.Item
            name="stock"
            label="库存数量"
            rules={[{ required: true, message: '请输入库存数量' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入库存数量"
              min={0}
            />
          </Form.Item>
          <Form.Item
            name="isDistributionEnabled"
            label="是否参与分销"
            valuePropName="checked"
          >
            <Switch checkedChildren="是" unCheckedChildren="否" />
          </Form.Item>
          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) => prevValues.isDistributionEnabled !== currentValues.isDistributionEnabled}
          >
            {({ getFieldValue }) =>
              getFieldValue('isDistributionEnabled') ? (
                <>
                  <Form.Item
                    name="firstLevelCommissionRate"
                    label="一级推广佣金比例"
                    rules={[{ required: true, message: '请输入一级推广佣金比例' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="请输入一级推广佣金比例，如 0.1 表示 10%"
                      min={0}
                      max={1}
                      step={0.01}
                      precision={4}
                      formatter={(value) => `${(value * 100).toFixed(2)}%`}
                      parser={(value) => parseFloat(value.replace('%', '')) / 100}
                    />
                  </Form.Item>
                  <Form.Item
                    name="secondLevelCommissionRate"
                    label="二级推广佣金比例"
                    rules={[{ required: true, message: '请输入二级推广佣金比例' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="请输入二级推广佣金比例，如 0.05 表示 5%"
                      min={0}
                      max={1}
                      step={0.01}
                      precision={4}
                      formatter={(value) => `${(value * 100).toFixed(2)}%`}
                      parser={(value) => parseFloat(value.replace('%', '')) / 100}
                    />
                  </Form.Item>
                </>
              ) : null
            }
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="编辑商品"
        open={editModalVisible}
        onOk={() => editForm.submit()}
        onCancel={() => {
          setEditModalVisible(false)
        }}
        width={600}
      >
        <Form form={editForm} layout="vertical" onFinish={handleEdit}>
          <Form.Item
            name="name"
            label="商品名称"
            rules={[{ required: true, message: '请输入商品名称' }]}
          >
            <Input placeholder="请输入商品名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="商品描述"
          >
            <Input.TextArea placeholder="请输入商品描述" rows={3} />
          </Form.Item>
          <Form.Item
            name="price"
            label="商品价格"
            rules={[{ required: true, message: '请输入商品价格' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入商品价格"
              min={0}
              precision={2}
              prefix="¥"
            />
          </Form.Item>
          <Form.Item
            name="stock"
            label="库存数量"
            rules={[{ required: true, message: '请输入库存数量' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="请输入库存数量"
              min={0}
            />
          </Form.Item>
          <Form.Item
            name="isDistributionEnabled"
            label="是否参与分销"
            valuePropName="checked"
          >
            <Switch checkedChildren="是" unCheckedChildren="否" />
          </Form.Item>
          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) => prevValues.isDistributionEnabled !== currentValues.isDistributionEnabled}
          >
            {({ getFieldValue }) =>
              getFieldValue('isDistributionEnabled') ? (
                <>
                  <Form.Item
                    name="firstLevelCommissionRate"
                    label="一级推广佣金比例"
                    rules={[{ required: true, message: '请输入一级推广佣金比例' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="请输入一级推广佣金比例，如 0.1 表示 10%"
                      min={0}
                      max={1}
                      step={0.01}
                      precision={4}
                      formatter={(value) => `${(value * 100).toFixed(2)}%`}
                      parser={(value) => parseFloat(value.replace('%', '')) / 100}
                    />
                  </Form.Item>
                  <Form.Item
                    name="secondLevelCommissionRate"
                    label="二级推广佣金比例"
                    rules={[{ required: true, message: '请输入二级推广佣金比例' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="请输入二级推广佣金比例，如 0.05 表示 5%"
                      min={0}
                      max={1}
                      step={0.01}
                      precision={4}
                      formatter={(value) => `${(value * 100).toFixed(2)}%`}
                      parser={(value) => parseFloat(value.replace('%', '')) / 100}
                    />
                  </Form.Item>
                </>
              ) : null
            }
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="商品详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedProduct && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="ID">{selectedProduct.id}</Descriptions.Item>
            <Descriptions.Item label="商品名称">{selectedProduct.name}</Descriptions.Item>
            <Descriptions.Item label="价格" span={2}>¥{selectedProduct.price}</Descriptions.Item>
            <Descriptions.Item label="库存">{selectedProduct.stock}</Descriptions.Item>
            <Descriptions.Item label="是否参与分销">
              <Tag color={selectedProduct.isDistributionEnabled ? 'green' : 'default'}>
                {selectedProduct.isDistributionEnabled ? '是' : '否'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="一级佣金比例">
              {(selectedProduct.firstLevelCommissionRate * 100).toFixed(2)}%
            </Descriptions.Item>
            <Descriptions.Item label="二级佣金比例">
              {(selectedProduct.secondLevelCommissionRate * 100).toFixed(2)}%
            </Descriptions.Item>
            <Descriptions.Item label="商品描述" span={2}>
              {selectedProduct.description || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {selectedProduct.createdAt ? dayjs(selectedProduct.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  )
}

export default Products
