import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Boxes, PackagePlus, Save, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm, type UseFormRegisterReturn } from 'react-hook-form'
import { z } from 'zod'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingBlock } from '../components/LoadingBlock'
import { StatusBadge } from '../components/StatusBadge'
import { getApiErrorMessage } from '../lib/apiClient'
import { eventCartApi } from '../lib/eventcartApi'
import { useSessionStore } from '../store/sessionStore'
import type { CreateProductRequest } from '../types/api'
import { formatDateTime } from '../utils/format'

const productSchema = z.object({
  sku: z.string().min(1, 'SKU is required'),
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  category: z.string().min(1, 'Category is required'),
  price: z.coerce.number().positive('Price must be greater than zero'),
  currency: z.string().min(3, 'Currency code is required').max(3, 'Use a three-letter code'),
  availableQuantity: z.coerce.number().int().min(0, 'Quantity cannot be negative'),
  tagsText: z.string().optional(),
})

const inventorySchema = z.object({
  productId: z.string().min(1, 'Product ID is required'),
  sku: z.string().min(1, 'SKU is required'),
  productName: z.string().min(1, 'Product name is required'),
  availableQuantity: z.coerce.number().int().min(0, 'Quantity cannot be negative'),
})

type ProductFormInput = z.input<typeof productSchema>
type ProductForm = z.output<typeof productSchema>
type InventoryFormInput = z.input<typeof inventorySchema>
type InventoryForm = z.output<typeof inventorySchema>

export function AdminPage() {
  const queryClient = useQueryClient()
  const selectedProductId = useSessionStore((state) => state.selectedProductId)
  const setSelectedProductId = useSessionStore((state) => state.setSelectedProductId)
  const [lookupProductId, setLookupProductId] = useState(selectedProductId)

  const productForm = useForm<ProductFormInput, unknown, ProductForm>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      sku: '',
      name: '',
      description: '',
      category: 'Electronics',
      price: 999,
      currency: 'INR',
      availableQuantity: 10,
      tagsText: 'demo, eventcart',
    },
  })

  const inventoryForm = useForm<InventoryFormInput, unknown, InventoryForm>({
    resolver: zodResolver(inventorySchema),
    defaultValues: {
      productId: selectedProductId,
      sku: '',
      productName: '',
      availableQuantity: 10,
    },
  })

  useEffect(() => {
    inventoryForm.setValue('productId', selectedProductId)
    setLookupProductId(selectedProductId)
  }, [inventoryForm, selectedProductId])

  const createProductMutation = useMutation({
    mutationFn: (form: ProductForm) => {
      const body: CreateProductRequest = {
        sku: form.sku,
        name: form.name,
        description: form.description,
        category: form.category,
        price: form.price,
        currency: form.currency.toUpperCase(),
        availableQuantity: form.availableQuantity,
        tags: (form.tagsText ?? '')
          .split(',')
          .map((tag) => tag.trim())
          .filter(Boolean),
      }
      return eventCartApi.createProduct(body)
    },
    onSuccess: (product) => {
      setSelectedProductId(product.id)
      inventoryForm.reset({
        productId: product.id,
        sku: product.sku,
        productName: product.name,
        availableQuantity: product.availableQuantity,
      })
      productForm.reset({
        sku: '',
        name: '',
        description: '',
        category: product.category,
        price: product.price,
        currency: product.currency,
        availableQuantity: product.availableQuantity,
        tagsText: product.tags.join(', '),
      })
      void queryClient.invalidateQueries({ queryKey: ['products'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-products'] })
    },
  })

  const inventoryMutation = useMutation({
    mutationFn: (form: InventoryForm) =>
      eventCartApi.upsertInventory(form.productId, {
        sku: form.sku,
        productName: form.productName,
        availableQuantity: form.availableQuantity,
      }),
    onSuccess: (inventory) => {
      setSelectedProductId(inventory.productId)
      setLookupProductId(inventory.productId)
      void queryClient.invalidateQueries({ queryKey: ['inventory', inventory.productId] })
    },
  })

  const inventoryQuery = useQuery({
    enabled: lookupProductId.length > 0,
    queryKey: ['inventory', lookupProductId],
    queryFn: () => eventCartApi.getInventory(lookupProductId),
    retry: false,
  })

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Admin APIs</p>
          <h1>Catalog and inventory setup</h1>
        </div>
      </div>

      <div className="split-layout">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Create product</h2>
              <p>Creates a MongoDB product document in catalog-service.</p>
            </div>
          </div>
          <form className="form-grid" onSubmit={productForm.handleSubmit((form) => createProductMutation.mutate(form))}>
            <FormInput label="SKU" registration={productForm.register('sku')} error={productForm.formState.errors.sku?.message} />
            <FormInput
              label="Name"
              registration={productForm.register('name')}
              error={productForm.formState.errors.name?.message}
            />
            <FormInput
              label="Category"
              registration={productForm.register('category')}
              error={productForm.formState.errors.category?.message}
            />
            <FormInput
              label="Price"
              registration={productForm.register('price')}
              error={productForm.formState.errors.price?.message}
              type="number"
            />
            <FormInput
              label="Currency"
              registration={productForm.register('currency')}
              error={productForm.formState.errors.currency?.message}
            />
            <FormInput
              label="Available quantity"
              registration={productForm.register('availableQuantity')}
              error={productForm.formState.errors.availableQuantity?.message}
              type="number"
            />
            <label className="field-block field-block--wide">
              Description
              <textarea rows={3} {...productForm.register('description')} />
            </label>
            <FormInput
              label="Tags"
              registration={productForm.register('tagsText')}
              error={productForm.formState.errors.tagsText?.message}
            />
            <button className="primary-button" disabled={createProductMutation.isPending} type="submit">
              <PackagePlus aria-hidden="true" size={18} />
              Create product
            </button>
          </form>
          {createProductMutation.isSuccess ? (
            <div className="success-note">Created product: {createProductMutation.data.id}</div>
          ) : null}
          {createProductMutation.isError ? <ErrorBanner message={getApiErrorMessage(createProductMutation.error)} /> : null}
        </section>

        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Inventory stock</h2>
              <p>Seeds inventory-service stock used by Kafka reservation events.</p>
            </div>
          </div>
          <form className="form-grid" onSubmit={inventoryForm.handleSubmit((form) => inventoryMutation.mutate(form))}>
            <FormInput
              label="Product ID"
              registration={inventoryForm.register('productId')}
              error={inventoryForm.formState.errors.productId?.message}
            />
            <FormInput
              label="SKU"
              registration={inventoryForm.register('sku')}
              error={inventoryForm.formState.errors.sku?.message}
            />
            <FormInput
              label="Product name"
              registration={inventoryForm.register('productName')}
              error={inventoryForm.formState.errors.productName?.message}
            />
            <FormInput
              label="Available quantity"
              registration={inventoryForm.register('availableQuantity')}
              error={inventoryForm.formState.errors.availableQuantity?.message}
              type="number"
            />
            <button className="primary-button" disabled={inventoryMutation.isPending} type="submit">
              <Save aria-hidden="true" size={18} />
              Save stock
            </button>
          </form>
          {inventoryMutation.isSuccess ? (
            <div className="success-note">Inventory saved for product: {inventoryMutation.data.productId}</div>
          ) : null}
          {inventoryMutation.isError ? <ErrorBanner message={getApiErrorMessage(inventoryMutation.error)} /> : null}

          <div className="lookup-box">
            <label>
              Inspect product stock
              <input onChange={(event) => setLookupProductId(event.target.value.trim())} value={lookupProductId} />
            </label>
            <button
              className="secondary-button"
              disabled={lookupProductId.length === 0}
              onClick={() => void inventoryQuery.refetch()}
              type="button"
            >
              <Search aria-hidden="true" size={18} />
              Lookup
            </button>
          </div>

          {inventoryQuery.isLoading ? <LoadingBlock label="Loading inventory" /> : null}
          {inventoryQuery.data ? (
            <dl className="summary-list">
              <div>
                <dt>Product</dt>
                <dd>{inventoryQuery.data.productName}</dd>
              </div>
              <div>
                <dt>Available</dt>
                <dd>{inventoryQuery.data.availableQuantity}</dd>
              </div>
              <div>
                <dt>Reserved</dt>
                <dd>{inventoryQuery.data.reservedQuantity}</dd>
              </div>
              <div>
                <dt>Updated</dt>
                <dd>{formatDateTime(inventoryQuery.data.updatedAt)}</dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>
                  <StatusBadge status={inventoryQuery.data.availableQuantity > 0 ? 'AVAILABLE' : 'EMPTY'} />
                </dd>
              </div>
            </dl>
          ) : null}
          {inventoryQuery.isError ? <ErrorBanner message={getApiErrorMessage(inventoryQuery.error)} /> : null}
          <div className="admin-note">
            <Boxes aria-hidden="true" size={18} />
            Save stock before order testing to avoid the intentional insufficient inventory path.
          </div>
        </section>
      </div>
    </section>
  )
}

type FormInputProps = {
  label: string
  registration: UseFormRegisterReturn
  error?: string
  type?: string
}

function FormInput({ label, registration, error, type = 'text' }: FormInputProps) {
  return (
    <label className="field-block">
      {label}
      <input type={type} {...registration} />
      {error ? <small>{error}</small> : null}
    </label>
  )
}
