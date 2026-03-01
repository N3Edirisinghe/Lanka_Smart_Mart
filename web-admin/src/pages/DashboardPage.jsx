import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import {
    LogOut, Plus, Trash2, Search, LayoutDashboard,
    Package, ShoppingCart, Users, Tag, AlertCircle, RefreshCw
} from 'lucide-react';

export default function DashboardPage() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('inventory');
    const [products, setProducts] = useState([]);
    const [orders, setOrders] = useState([]);
    const [users, setUsers] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // Modal State
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [newProduct, setNewProduct] = useState({
        name: '',
        description: '',
        price: '',
        oldPrice: '',
        imageUrl: '',
        category: '',
        stockQuantity: '10',
    });
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (activeTab === 'inventory' && products.length === 0) fetchProducts();
        else if (activeTab === 'orders' && orders.length === 0) fetchOrders();
        else if (activeTab === 'users' && users.length === 0) fetchUsers();
    }, [activeTab]);

    const fetchProducts = async () => {
        try {
            setIsLoading(true);
            const res = await fetch('https://lanka-smart-mart.vercel.app/api/products', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('adminToken')}`
                }
            });
            const data = await res.json();
            if (data.success) {
                setProducts(data.products);
            } else {
                setError(data.error || 'System failed to retrieve inventory');
            }
        } catch (err) {
            setError('Connection timeout. Gateway unavailable.');
        } finally {
            setIsLoading(false);
        }
    };

    const fetchOrders = async () => {
        try {
            setIsLoading(true);
            const res = await fetch('https://lanka-smart-mart.vercel.app/api/orders', {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('adminToken')}` }
            });
            const data = await res.json();
            if (data.success) {
                setOrders(data.orders);
            } else {
                setError(data.error || 'System failed to retrieve orders');
            }
        } catch (err) {
            setError('Connection timeout. Gateway unavailable.');
        } finally {
            setIsLoading(false);
        }
    };

    const fetchUsers = async () => {
        try {
            setIsLoading(true);
            const res = await fetch('https://lanka-smart-mart.vercel.app/api/users', {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('adminToken')}` }
            });
            const data = await res.json();
            if (data.success) {
                setUsers(data.users);
            } else {
                setError(data.error || 'System failed to retrieve users');
            }
        } catch (err) {
            setError('Connection timeout. Gateway unavailable.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleLogout = () => {
        sessionStorage.removeItem('adminToken');
        navigate('/admin');
    };

    const handleDelete = async (productId) => {
        if (!window.confirm("CONFIRM DATABASE DELETION: Are you sure you want to permanently erase this record?")) return;

        try {
            const token = sessionStorage.getItem('adminToken');
            const res = await fetch(`https://lanka-smart-mart.vercel.app/api/products/${productId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const data = await res.json();
            if (data.success) {
                setProducts(products.filter(p => p.id !== productId));
            } else {
                alert(data.error || 'Deletion protocol failed');
            }
        } catch (err) {
            alert("Network disruption during deletion");
        }
    };

    const handleDeleteUser = async (userId) => {
        if (!window.confirm("CONFIRM NODE DELETION: Are you sure you want to permanently revoke access for this user?")) return;

        try {
            const token = sessionStorage.getItem('adminToken');
            const res = await fetch(`https://lanka-smart-mart.vercel.app/api/users/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const data = await res.json();
            if (data.success) {
                setUsers(users.filter(u => u.id !== userId));
            } else {
                alert(data.error || 'User deletion protocol failed');
            }
        } catch (err) {
            alert("Network disruption during user deletion");
        }
    };

    const handleAddSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const token = sessionStorage.getItem('adminToken');
            const payload = {
                ...newProduct,
                price: parseFloat(newProduct.price),
                oldPrice: newProduct.oldPrice ? parseFloat(newProduct.oldPrice) : 0,
                stockQuantity: parseInt(newProduct.stockQuantity)
            };

            const res = await fetch('https://lanka-smart-mart.vercel.app/api/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });
            const data = await res.json();

            if (data.success) {
                setProducts([{ id: data.productId, ...payload }, ...products]);
                setIsAddModalOpen(false);
                setNewProduct({ name: '', description: '', price: '', oldPrice: '', imageUrl: '', category: '', stockQuantity: '10' });
            } else {
                alert(data.error || 'Insertion protocol failed');
            }
        } catch (err) {
            alert("Network disruption during payload insertion");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-slate-200 flex font-sans selection:bg-emerald-500/30">

            {/* Sidebar Focus Area */}
            <aside className="w-72 bg-slate-950 text-slate-400 hidden lg:flex flex-col border-r border-white/5 relative z-20">
                <div className="h-20 flex items-center px-8 border-b border-white/5 bg-slate-900/30">
                    <div className="w-8 h-8 rounded-lg bg-emerald-500/20 flex items-center justify-center mr-3 shadow-[0_0_15px_rgba(16,185,129,0.2)] border border-emerald-500/30">
                        <LayoutDashboard className="w-4 h-4 text-emerald-400" />
                    </div>
                    <span className="text-xl font-bold tracking-tight text-white">Console</span>
                </div>

                <nav className="flex-1 px-4 py-8 space-y-3">
                    <p className="px-4 text-xs font-semibold uppercase tracking-widest text-slate-600 mb-4">Core Modules</p>
                    <button onClick={() => setActiveTab('inventory')} className={`w-full group flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${activeTab === 'inventory' ? 'bg-emerald-500/10 text-emerald-400 font-medium border border-emerald-500/20 shadow-[inset_0_1px_0_rgba(255,255,255,0.05)]' : 'hover:bg-slate-900 hover:text-white border border-transparent hover:border-white/5'}`}>
                        <Package className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        Inventory Matrix
                        {activeTab === 'inventory' && <div className="ml-auto w-1.5 h-1.5 rounded-full bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.8)]" />}
                    </button>
                    <button onClick={() => setActiveTab('orders')} className={`w-full group flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${activeTab === 'orders' ? 'bg-emerald-500/10 text-emerald-400 font-medium border border-emerald-500/20 shadow-[inset_0_1px_0_rgba(255,255,255,0.05)]' : 'hover:bg-slate-900 hover:text-white border border-transparent hover:border-white/5'}`}>
                        <ShoppingCart className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        Active Orders
                        {activeTab === 'orders' && <div className="ml-auto w-1.5 h-1.5 rounded-full bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.8)]" />}
                    </button>
                    <button onClick={() => setActiveTab('users')} className={`w-full group flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${activeTab === 'users' ? 'bg-emerald-500/10 text-emerald-400 font-medium border border-emerald-500/20 shadow-[inset_0_1px_0_rgba(255,255,255,0.05)]' : 'hover:bg-slate-900 hover:text-white border border-transparent hover:border-white/5'}`}>
                        <Users className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        Customer Nodes
                        {activeTab === 'users' && <div className="ml-auto w-1.5 h-1.5 rounded-full bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.8)]" />}
                    </button>
                </nav>

                <div className="p-6 border-t border-white/5 bg-slate-900/20">
                    <div className="flex items-center gap-3 mb-6 px-2">
                        <div className="w-10 h-10 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center">
                            <span className="text-emerald-400 font-bold text-sm">AD</span>
                        </div>
                        <div>
                            <p className="text-sm font-semibold text-white">System Admin</p>
                            <p className="text-xs text-emerald-500 flex items-center gap-1"><span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" /> Online</p>
                        </div>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="group flex items-center justify-center gap-2 w-full py-3 px-4 rounded-xl font-semibold text-slate-300 bg-slate-900 hover:bg-red-500/10 hover:text-red-400 transition-all border border-white/5 hover:border-red-500/30"
                    >
                        <LogOut className="w-4 h-4 group-hover:-translate-x-1 transition-transform" /> Disconnect
                    </button>
                </div>
            </aside>

            {/* Main Workspace */}
            <main className="flex-1 flex flex-col min-w-0 bg-slate-900 relative">
                {/* Subtle Background Elements in Workspace */}
                <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-emerald-500/5 rounded-full blur-[120px] pointer-events-none" />

                <header className="h-20 bg-slate-950/50 backdrop-blur-md border-b border-white/5 flex items-center justify-between px-8 shrink-0 sticky top-0 z-10">
                    <div className="flex items-center gap-4">
                        <LayoutDashboard className="w-6 h-6 text-emerald-500 lg:hidden" />
                        <div>
                            <h1 className="text-xl font-bold text-white tracking-tight">
                                {activeTab === 'inventory' ? 'Inventory Management' : activeTab === 'orders' ? 'Order Processing' : 'User Security & Access'}
                            </h1>
                            <p className="text-xs text-slate-500 font-medium">Real-time sync to remote database</p>
                        </div>
                    </div>
                    <div className="flex items-center gap-6">
                        <div className="relative hidden md:block group">
                            <Search className="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-emerald-400 transition-colors" />
                            <input
                                type="text"
                                placeholder="Search identifiers..."
                                className="pl-11 pr-4 py-2.5 bg-slate-900 border border-white/10 rounded-full text-sm text-white focus:ring-1 focus:ring-emerald-500/50 outline-none w-72 placeholder-slate-600 transition-all focus:bg-slate-950"
                            />
                        </div>
                    </div>
                </header>

                <div className="flex-1 overflow-auto p-8 custom-scrollbar relative z-0">
                    <div className="max-w-7xl mx-auto">

                        {/* Header / Action Controls */}
                        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6 mb-10 bg-slate-950/30 p-6 rounded-3xl border border-white/5 shadow-lg">
                            <div>
                                <h2 className="text-3xl font-bold text-white tracking-tight mb-2">
                                    {activeTab === 'inventory' ? 'Total Commodities' : activeTab === 'orders' ? 'Active Orders' : 'Registered Nodes'}
                                    <span className="text-emerald-500 ml-2">[{activeTab === 'inventory' ? products.length : activeTab === 'orders' ? orders.length : users.length}]</span>
                                </h2>
                                <div className="flex items-center gap-3 text-sm text-slate-400">
                                    <span className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-900 border border-white/10"><Tag className="w-3.5 h-3.5 text-emerald-400" /> Catalog Active</span>
                                    <span className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-900 border border-white/10"><RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-emerald-400' : 'text-slate-500'}`} /> {isLoading ? 'Syncing...' : 'Synced'}</span>
                                </div>
                            </div>
                            {activeTab === 'inventory' && (
                                <button
                                    onClick={() => setIsAddModalOpen(true)}
                                    className="group flex justify-center items-center gap-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 px-6 py-3 rounded-xl font-bold transition-all shadow-[0_0_20px_rgba(16,185,129,0.3)] hover:shadow-[0_0_30px_rgba(16,185,129,0.5)] transform hover:-translate-y-0.5"
                                >
                                    <Plus className="w-5 h-5" /> Initialize Record
                                </button>
                            )}
                        </div>

                        {/* Error State */}
                        {error && (
                            <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8 p-4 bg-red-500/10 border border-red-500/20 rounded-2xl flex items-center gap-4 text-red-400 shadow-lg shadow-red-500/5">
                                <AlertCircle className="w-6 h-6 shrink-0 text-red-500" />
                                <p className="font-medium text-sm">{error}</p>
                                <button onClick={() => activeTab === 'inventory' ? fetchProducts() : activeTab === 'orders' ? fetchOrders() : fetchUsers()} className="ml-auto px-4 py-2 bg-red-500/20 hover:bg-red-500/30 rounded-lg text-xs font-bold uppercase tracking-wider transition-colors">Re-Establish Link</button>
                            </motion.div>
                        )}

                        {/* Data Table Container */}
                        <div className="bg-slate-950 rounded-[2rem] shadow-2xl border border-white/10 overflow-hidden relative">
                            <div className="absolute top-0 inset-x-0 h-px bg-gradient-to-r from-transparent via-emerald-500/20 to-transparent" />

                            <div className="overflow-x-auto min-h-[400px]">
                                <table className="w-full text-left border-collapse whitespace-nowrap">
                                    <thead>
                                        <tr className="bg-slate-900 text-slate-400 text-xs font-bold uppercase tracking-widest border-b border-white/5">
                                            {activeTab === 'inventory' && (
                                                <>
                                                    <th className="px-8 py-5">Product Identifier</th>
                                                    <th className="px-6 py-5">Category Label</th>
                                                    <th className="px-6 py-5">Market Value</th>
                                                    <th className="px-6 py-5">Inventory Qty</th>
                                                    <th className="px-8 py-5 text-right">Admin Exec</th>
                                                </>
                                            )}
                                            {activeTab === 'orders' && (
                                                <>
                                                    <th className="px-8 py-5">Order ID</th>
                                                    <th className="px-6 py-5">Customer Node</th>
                                                    <th className="px-6 py-5">Financial Value</th>
                                                    <th className="px-6 py-5">Payment Method</th>
                                                    <th className="px-8 py-5 text-right">Timestamp</th>
                                                </>
                                            )}
                                            {activeTab === 'users' && (
                                                <>
                                                    <th className="px-8 py-5">User UID</th>
                                                    <th className="px-6 py-5">Email Address</th>
                                                    <th className="px-6 py-5">Display Name</th>
                                                    <th className="px-6 py-5 text-right">Registered At</th>
                                                    <th className="px-8 py-5 text-right">Admin Exec</th>
                                                </>
                                            )}
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-white/5">
                                        {isLoading ? (
                                            <tr>
                                                <td colSpan="5" className="px-8 py-24 text-center">
                                                    <div className="flex flex-col items-center justify-center gap-4">
                                                        <div className="w-10 h-10 border-2 border-slate-700 border-t-emerald-500 rounded-full animate-spin"></div>
                                                        <p className="text-slate-500 text-sm tracking-wide">Acquiring database index...</p>
                                                    </div>
                                                </td>
                                            </tr>
                                        ) : (
                                            (activeTab === 'inventory' && products.length === 0) ||
                                            (activeTab === 'orders' && orders.length === 0) ||
                                            (activeTab === 'users' && users.length === 0)
                                        ) ? (
                                            <tr>
                                                <td colSpan="5" className="px-8 py-24 text-center">
                                                    <div className="max-w-xs mx-auto flex flex-col items-center">
                                                        <div className="w-20 h-20 bg-slate-900 rounded-3xl border border-white/5 flex items-center justify-center mb-6 shadow-inner">
                                                            {activeTab === 'inventory' ? <Package className="w-10 h-10 text-slate-700" /> : activeTab === 'orders' ? <ShoppingCart className="w-10 h-10 text-slate-700" /> : <Users className="w-10 h-10 text-slate-700" />}
                                                        </div>
                                                        <p className="text-slate-300 font-semibold text-lg mb-2">Zero Records Found</p>
                                                        <p className="text-slate-500 text-sm leading-relaxed mb-6">The remote database cluster returned an empty payload.</p>
                                                        {activeTab === 'inventory' && <button onClick={() => setIsAddModalOpen(true)} className="text-emerald-400 text-sm font-bold hover:text-emerald-300 transition-colors uppercase tracking-wider flex items-center gap-2"><Plus className="w-4 h-4" /> Initialize Now</button>}
                                                    </div>
                                                </td>
                                            </tr>
                                        ) : (
                                            <AnimatePresence>
                                                {activeTab === 'inventory' && products.map((product) => (
                                                    <motion.tr
                                                        key={product.id}
                                                        initial={{ opacity: 0, scale: 0.98 }}
                                                        animate={{ opacity: 1, scale: 1 }}
                                                        exit={{ opacity: 0, filter: 'blur(5px)', transition: { duration: 0.2 } }}
                                                        className="hover:bg-slate-800/50 transition-colors group relative border-b border-white/5 last:border-0"
                                                    >
                                                        <td className="px-8 py-5">
                                                            <div className="flex items-center gap-5">
                                                                <div className="h-14 w-14 rounded-2xl bg-slate-900 border border-white/10 overflow-hidden shrink-0 flex items-center justify-center shadow-md relative group-hover:shadow-emerald-500/20 transition-all">
                                                                    {product.imageUrl ? (
                                                                        <>
                                                                            <div className="absolute inset-0 bg-slate-900/10 group-hover:bg-transparent transition-colors z-10" />
                                                                            <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover transform group-hover:scale-110 transition-transform duration-500" />
                                                                        </>
                                                                    ) : (
                                                                        <Package className="w-6 h-6 text-slate-600" />
                                                                    )}
                                                                </div>
                                                                <div>
                                                                    <div className="font-bold text-slate-200 tracking-tight text-[15px] group-hover:text-white transition-colors">{product.name}</div>
                                                                    <div className="text-xs text-slate-500 truncate max-w-[280px] mt-1 pr-4">{product.description || 'Description payload empty'}</div>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td className="px-6 py-5">
                                                            <span className="inline-flex items-center px-3 py-1 rounded-lg text-xs font-semibold bg-slate-900 border border-white/5 text-slate-300 shadow-sm">
                                                                <Tag className="w-3.5 h-3.5 mr-2 text-slate-500" /> {product.category || 'N/A'}
                                                            </span>
                                                        </td>
                                                        <td className="px-6 py-5">
                                                            <div className="flex items-center gap-3">
                                                                <div className="font-bold text-emerald-400">Rs. {Number(product.price).toFixed(2)}</div>
                                                                {product.oldPrice > 0 && (
                                                                    <div className="text-xs text-slate-600 line-through font-medium">Rs. {Number(product.oldPrice).toFixed(2)}</div>
                                                                )}
                                                            </div>
                                                        </td>
                                                        <td className="px-6 py-5">
                                                            <div className="flex items-center gap-2">
                                                                <div className={`w-2 h-2 rounded-full ${product.stockQuantity > 5 ? 'bg-emerald-500' : product.stockQuantity > 0 ? 'bg-amber-500' : 'bg-red-500'} shadow-[0_0_8px_currentColor]`} />
                                                                <span className="text-sm font-semibold text-slate-300">{product.stockQuantity || 0} Vol</span>
                                                            </div>
                                                        </td>
                                                        <td className="px-8 py-5 text-right">
                                                            <button
                                                                onClick={() => handleDelete(product.id)}
                                                                className="p-2.5 text-slate-500 hover:text-red-400 hover:bg-slate-900 border border-transparent hover:border-red-500/30 rounded-xl transition-all opacity-0 group-hover:opacity-100 focus:opacity-100"
                                                                title="Purge Record"
                                                            >
                                                                <Trash2 className="w-5 h-5" />
                                                            </button>
                                                        </td>
                                                    </motion.tr>
                                                ))}

                                                {activeTab === 'orders' && orders.map((order) => (
                                                    <motion.tr
                                                        key={order.id}
                                                        initial={{ opacity: 0, scale: 0.98 }}
                                                        animate={{ opacity: 1, scale: 1 }}
                                                        exit={{ opacity: 0 }}
                                                        className="hover:bg-slate-800/50 transition-colors group relative border-b border-white/5 last:border-0"
                                                    >
                                                        <td className="px-8 py-5 font-mono text-sm text-slate-300">{order.id}</td>
                                                        <td className="px-6 py-5 text-sm text-slate-300">{order.email || 'N/A'}</td>
                                                        <td className="px-6 py-5 text-sm font-bold text-emerald-400">Rs. {Number(order.totalPrice || 0).toFixed(2)}</td>
                                                        <td className="px-6 py-5">
                                                            <span className={`px-3 py-1 text-xs font-semibold rounded-lg ${order.paymentMethod === 'Cash on Delivery' ? 'bg-amber-500/10 text-amber-500 border border-amber-500/20' : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'}`}>
                                                                {order.paymentMethod || 'Online'}
                                                            </span>
                                                        </td>
                                                        <td className="px-8 py-5 text-right text-xs text-slate-500">
                                                            {order.createdAt ? new Date(order.createdAt).toLocaleString() : 'N/A'}
                                                        </td>
                                                    </motion.tr>
                                                ))}

                                                {activeTab === 'users' && users.map((user) => (
                                                    <motion.tr
                                                        key={user.id}
                                                        initial={{ opacity: 0, scale: 0.98 }}
                                                        animate={{ opacity: 1, scale: 1 }}
                                                        exit={{ opacity: 0 }}
                                                        className="hover:bg-slate-800/50 transition-colors group relative border-b border-white/5 last:border-0"
                                                    >
                                                        <td className="px-8 py-5 font-mono text-sm text-slate-300">{user.id}</td>
                                                        <td className="px-6 py-5 text-sm text-slate-300 flex items-center gap-3">
                                                            <div className="w-8 h-8 rounded-full bg-indigo-500/20 text-indigo-400 flex items-center justify-center font-bold text-xs">
                                                                {user.email ? user.email.charAt(0).toUpperCase() : 'U'}
                                                            </div>
                                                            {user.email || 'N/A'}
                                                        </td>
                                                        <td className="px-6 py-5 text-sm font-medium text-white">{user.name || 'Anonymous Node'}</td>
                                                        <td className="px-6 py-5 text-right text-xs text-slate-500">
                                                            {user.createdAt ? new Date(user.createdAt).toLocaleString() : 'N/A'}
                                                        </td>
                                                        <td className="px-8 py-5 text-right">
                                                            <button
                                                                onClick={() => handleDeleteUser(user.id)}
                                                                className="p-2.5 text-slate-500 hover:text-red-400 hover:bg-slate-900 border border-transparent hover:border-red-500/30 rounded-xl transition-all opacity-0 group-hover:opacity-100 focus:opacity-100"
                                                                title="Revoke Node Access"
                                                            >
                                                                <Trash2 className="w-5 h-5" />
                                                            </button>
                                                        </td>
                                                    </motion.tr>
                                                ))}
                                            </AnimatePresence>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                    </div>
                </div>
            </main>

            {/* Add Product Modal Overlay */}
            <AnimatePresence>
                {isAddModalOpen && (
                    <>
                        <motion.div
                            initial={{ opacity: 0, backdropFilter: "blur(0px)" }}
                            animate={{ opacity: 1, backdropFilter: "blur(8px)" }}
                            exit={{ opacity: 0, backdropFilter: "blur(0px)", transition: { duration: 0.3 } }}
                            onClick={() => setIsAddModalOpen(false)}
                            className="fixed inset-0 bg-slate-950/60 z-40"
                        />
                        <motion.div
                            initial={{ opacity: 0, scale: 0.95, y: 30, rotateX: 10 }}
                            animate={{ opacity: 1, scale: 1, y: 0, rotateX: 0 }}
                            exit={{ opacity: 0, scale: 0.95, y: 20, rotateX: -10 }}
                            transition={{ type: "spring", stiffness: 300, damping: 25 }}
                            style={{ transformPerspective: 1200 }}
                            className="fixed inset-0 m-auto w-full max-w-2xl h-fit max-h-[90vh] bg-slate-900 border border-white/10 rounded-[2rem] shadow-[0_30px_100px_rgba(0,0,0,0.8)] z-50 flex flex-col overflow-hidden"
                        >
                            {/* Header */}
                            <div className="px-8 py-6 border-b border-white/5 bg-slate-950/50 flex justify-between items-center relative overflow-hidden">
                                <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-500/5 rounded-full blur-[80px]" />
                                <div className="relative z-10">
                                    <h3 className="text-2xl font-bold text-white tracking-tight flex items-center gap-3">
                                        <div className="w-8 h-8 rounded-lg bg-emerald-500/20 flex items-center justify-center border border-emerald-500/30">
                                            <Plus className="w-5 h-5 text-emerald-400" />
                                        </div>
                                        Record Intialization
                                    </h3>
                                    <p className="text-xs text-slate-500 mt-1 uppercase tracking-wider font-semibold">Database Schema: Product</p>
                                </div>
                                <button
                                    onClick={() => setIsAddModalOpen(false)}
                                    className="w-10 h-10 bg-slate-800 hover:bg-slate-700 rounded-full flex items-center justify-center text-slate-400 hover:text-white transition-colors relative z-10"
                                >
                                    ✕
                                </button>
                            </div>

                            {/* Form Body */}
                            <div className="p-8 overflow-y-auto custom-scrollbar relative">
                                <form id="addProductForm" onSubmit={handleAddSubmit} className="space-y-6">

                                    <div className="space-y-2 relative group">
                                        <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">Commodity Designation</label>
                                        <input required type="text" value={newProduct.name} onChange={e => setNewProduct({ ...newProduct, name: e.target.value })} className="w-full px-5 py-3.5 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 text-white placeholder-slate-600 outline-none transition-all" placeholder="e.g. Organic Avocados - Batch #2" />
                                        <div className="absolute inset-x-0 bottom-0 h-0.5 rounded-full bg-emerald-500/0 group-focus-within:bg-emerald-500/50 pointer-events-none transition-all duration-500" />
                                    </div>

                                    <div className="grid grid-cols-2 gap-6">
                                        <div className="space-y-2 relative group">
                                            <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">List Price (LKR)</label>
                                            <input required type="number" step="0.01" value={newProduct.price} onChange={e => setNewProduct({ ...newProduct, price: e.target.value })} className="w-full px-5 py-3.5 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 text-emerald-100 placeholder-slate-600 outline-none transition-all font-mono" placeholder="0.00" />
                                        </div>
                                        <div className="space-y-2 relative group">
                                            <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">Legacy Price (LKR)</label>
                                            <input type="number" step="0.01" value={newProduct.oldPrice} onChange={e => setNewProduct({ ...newProduct, oldPrice: e.target.value })} className="w-full px-5 py-3.5 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 text-slate-400 placeholder-slate-600 outline-none transition-all font-mono" placeholder="Optional" />
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-2 gap-6">
                                        <div className="space-y-2 relative group">
                                            <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">Taxonomy Label</label>
                                            <input required type="text" value={newProduct.category} onChange={e => setNewProduct({ ...newProduct, category: e.target.value })} className="w-full px-5 py-3.5 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 text-white placeholder-slate-600 outline-none transition-all" placeholder="e.g. Fresh Produce" />
                                        </div>
                                        <div className="space-y-2 relative group">
                                            <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">Initial Volume (Units)</label>
                                            <input required type="number" value={newProduct.stockQuantity} onChange={e => setNewProduct({ ...newProduct, stockQuantity: e.target.value })} className="w-full px-5 py-3.5 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 text-white placeholder-slate-600 outline-none transition-all font-mono" />
                                        </div>
                                    </div>

                                    <div className="space-y-2 relative group">
                                        <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">Asset CDN URL</label>
                                        <input required type="url" value={newProduct.imageUrl} onChange={e => setNewProduct({ ...newProduct, imageUrl: e.target.value })} className="w-full px-5 py-3.5 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 text-cyan-200 placeholder-slate-600 outline-none transition-all font-mono text-sm" placeholder="https://example.com/asset-path.jpg" />
                                    </div>

                                    <div className="space-y-2 relative group">
                                        <label className="text-xs font-semibold text-slate-400 tracking-wider uppercase ml-1 block">Payload Descriptor</label>
                                        <textarea rows="4" value={newProduct.description} onChange={e => setNewProduct({ ...newProduct, description: e.target.value })} className="w-full px-5 py-4 bg-slate-950/50 border border-white/10 rounded-2xl focus:ring-2 focus:ring-emerald-500/50 text-slate-300 placeholder-slate-600 outline-none transition-all resize-none" placeholder="Enter detailed commodity specifications..."></textarea>
                                    </div>
                                </form>
                            </div>

                            {/* Footer Controls */}
                            <div className="px-8 py-6 border-t border-white/5 bg-slate-950/80 flex justify-end gap-4 shadow-[0_-10px_20px_rgba(0,0,0,0.2)]">
                                <button type="button" onClick={() => setIsAddModalOpen(false)} className="px-6 py-3 text-sm font-bold text-slate-300 hover:text-white hover:bg-slate-800 rounded-xl transition-colors border border-transparent hover:border-white/10 tracking-wide">
                                    ABORT
                                </button>
                                <button type="submit" form="addProductForm" disabled={isSubmitting} className="px-8 py-3 text-sm font-bold text-slate-950 bg-emerald-400 hover:bg-emerald-300 rounded-xl transition-all disabled:opacity-50 flex items-center gap-2 shadow-[0_0_20px_rgba(16,185,129,0.2)] hover:shadow-[0_0_30px_rgba(16,185,129,0.4)] tracking-wide">
                                    {isSubmitting ? <span className="w-5 h-5 border-2 border-slate-900/30 border-t-slate-900 rounded-full animate-spin"></span> : 'EXECUTE INSERTION'}
                                </button>
                            </div>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>

        </div>
    );
}
