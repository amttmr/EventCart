import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PackagePlus, PackageSearch, Search, ShoppingCart } from 'lucide-react'
import { useMemo, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/useAuth'
import { EmptyState } from '../components/EmptyState'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingBlock } from '../components/LoadingBlock'
import { StatusBadge } from '../components/StatusBadge'
import { getApiErrorMessage } from '../lib/apiClient'
import { eventCartApi, type ProductSearchParams } from '../lib/eventcartApi'
import { useSessionStore } from '../store/sessionStore'
import type { Product } from '../types/api'
import { formatCurrency } from '../utils/format'

export function CatalogPage() {
  const auth = useAuth()
  const queryClient = useQueryClient()
  const activeCustomerId = useSessionStore((state) => state.activeCustomerId)
  const setSelectedProductId = useSessionStore((state) => state.setSelectedProductId)
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [activeOnly, setActiveOnly] = useState(true)
  const [filters, setFilters] = useState<ProductSearchParams>({ active: true, size: 20 })
  const [quantities, setQuantities] = useState<Record<string, number>>({})

  const productsQuery = useQuery({
    queryKey: ['products', filters],
    queryFn: () => eventCartApi.searchProducts(filters),
  })

  const addMutation = useMutation({
    mutationFn: ({ productId, quantity }: { productId: string; quantity: number }) =>
      eventCartApi.addCartItem(activeCustomerId, productId, quantity),
    onSuccess: (_cart, variables) => {
      setSelectedProductId(variables.productId)
      void queryClient.invalidateQueries({ queryKey: ['cart', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-cart', activeCustomerId] })
    },
  })

  const categoryOptions = useMemo(() => {
    const categories = productsQuery.data?.content.map((product) => product.category) ?? []
    return Array.from(new Set(categories)).sort()
  }, [productsQuery.data?.content])

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFilters({
      keyword: keyword.trim() || undefined,
      category: category.trim() || undefined,
      active: activeOnly ? true : undefined,
      size: 20,
    })
  }

  function updateQuantity(productId: string, quantity: number) {
    setQuantities((current) => ({ ...current, [productId]: Math.max(1, quantity) }))
  }

  function addProduct(product: Product) {
    const quantity = quantities[product.id] ?? 1
    addMutation.mutate({ productId: product.id, quantity })
  }

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Catalog-service</p>
          <h1>Product catalog</h1>
        </div>
      </div>

      <section className="panel">
        <form className="toolbar-form" onSubmit={handleSearch}>
          <label>
            Search
            <input
              onChange={(event) => setKeyword(event.target.value)}
              placeholder="keyboard, java, rgb"
              value={keyword}
            />
          </label>
          <label>
            Category
            <input
              list="category-options"
              onChange={(event) => setCategory(event.target.value)}
              placeholder="Electronics"
              value={category}
            />
          </label>
          <datalist id="category-options">
            {categoryOptions.map((option) => (
              <option key={option} value={option} />
            ))}
          </datalist>
          <label className="checkbox-field">
            <input
              checked={activeOnly}
              onChange={(event) => setActiveOnly(event.target.checked)}
              type="checkbox"
            />
            Active only
          </label>
          <button className="primary-button" type="submit">
            <Search aria-hidden="true" size={18} />
            Search
          </button>
        </form>
      </section>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Products</h2>
            <p>Add items to the current customer cart from catalog snapshots.</p>
          </div>
          <span className="result-count">{productsQuery.data?.totalElements ?? 0} results</span>
        </div>

        {productsQuery.isLoading ? <LoadingBlock /> : null}
        {productsQuery.isError ? (
          <ErrorBanner
            message={getApiErrorMessage(productsQuery.error)}
            onRetry={() => void productsQuery.refetch()}
          />
        ) : null}
        {!productsQuery.isLoading && productsQuery.data?.content.length === 0 ? (
          <EmptyState
            icon={<PackageSearch size={24} />}
            title="No products found"
            message="Create products from the Admin screen or loosen the current filters."
          />
        ) : null}

        <div className="product-grid">
          {productsQuery.data?.content.map((product) => (
            <article className="product-tile" key={product.id}>
              <div className="product-tile__main">
                <div>
                  <h3>{product.name}</h3>
                  <p>{product.description || 'No description provided'}</p>
                </div>
                <StatusBadge status={product.active ? 'ACTIVE' : 'INACTIVE'} />
              </div>
              <dl className="compact-facts">
                <div>
                  <dt>SKU</dt>
                  <dd>{product.sku}</dd>
                </div>
                <div>
                  <dt>Price</dt>
                  <dd>{formatCurrency(product.price, product.currency)}</dd>
                </div>
                <div>
                  <dt>Stock view</dt>
                  <dd>{product.availableQuantity}</dd>
                </div>
                <div>
                  <dt>Category</dt>
                  <dd>{product.category}</dd>
                </div>
              </dl>
              <div className="tag-list">
                {product.tags.map((tag) => (
                  <span key={tag}>{tag}</span>
                ))}
              </div>
              <div className="tile-actions">
                <input
                  aria-label={`Quantity for ${product.name}`}
                  min={1}
                  onChange={(event) => updateQuantity(product.id, Number(event.target.value))}
                  type="number"
                  value={quantities[product.id] ?? 1}
                />
                <button
                  className="secondary-button"
                  disabled={!auth.isAuthenticated || addMutation.isPending || !product.active}
                  onClick={() => addProduct(product)}
                  type="button"
                >
                  <ShoppingCart aria-hidden="true" size={18} />
                  Add
                </button>
                <button
                  className="icon-button"
                  onClick={() => setSelectedProductId(product.id)}
                  title="Use product in admin inventory form"
                  type="button"
                >
                  <PackagePlus aria-hidden="true" size={18} />
                </button>
              </div>
            </article>
          ))}
        </div>

        {addMutation.isError ? <ErrorBanner message={getApiErrorMessage(addMutation.error)} /> : null}
      </section>
    </section>
  )
}
