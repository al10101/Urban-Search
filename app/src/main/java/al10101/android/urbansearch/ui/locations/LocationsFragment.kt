package al10101.android.urbansearch.ui.locations

import al10101.android.urbansearch.Location
import al10101.android.urbansearch.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import al10101.android.urbansearch.databinding.FragmentLocationsBinding
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "LocationsFragment"

class LocationsFragment : Fragment() {

    private lateinit var locationsViewModel: LocationsViewModel
    private var _binding: FragmentLocationsBinding? = null

    private var locationAdapter: LocationAdapter? = LocationAdapter(emptyList())

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locationsViewModel =
            ViewModelProvider(this).get(LocationsViewModel::class.java)

        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = locationAdapter
        }

        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationsViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            { locations ->
                locations?.let {
                    if (locations.isNotEmpty()) {
                        binding.emptyListTextView.visibility = View.GONE
                    } else {
                        binding.emptyListTextView.visibility = View.VISIBLE
                    }
                    Log.i(TAG, "Got locations ${locations.size}")
                    updateUI(locations)
                }
            }
        )
    }

    private fun updateUI(locations: List<Location>) {
        locationAdapter = LocationAdapter(locations)
        binding.recyclerView.adapter = locationAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class LocationHolder(view: View): RecyclerView.ViewHolder(view) {

        private lateinit var location: Location

        private val titleTextView: TextView = itemView.findViewById(R.id.location_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.location_date)

        fun bind(location: Location) {
            this.location = location
            titleTextView.text = location.title
            dateTextView.text = location.date.toString()
        }

    }

    private inner class LocationAdapter(var locations: List<Location>)
        : RecyclerView.Adapter<LocationHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
            val view = layoutInflater.inflate(R.layout.list_item_location, parent, false)
            return LocationHolder(view)
        }

        override fun onBindViewHolder(holder: LocationHolder, position: Int) {
            val location = locations[position]
            holder.bind(location)
        }

        override fun getItemCount() = locations.size


    }

}